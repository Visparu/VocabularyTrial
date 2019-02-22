package com.visparu.vocabularytrial.model.db.entities;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.visparu.vocabularytrial.model.db.ConnectionDetails;

public final class Word
{
	
	private static final Map<Integer, Word> cache = new HashMap<>();
	
	private Integer		word_id;
	private String		name;
	private Language	language;
	
	private Word(final Integer word_id, final String name, final Language language)
	{
		LogItem.enter();
		this.word_id	= word_id;
		this.name		= name;
		this.language	= language;
		LogItem.exit();
	}
	
	public static final void createTable()
	{
		LogItem.enter();
		final String	query		= "CREATE TABLE IF NOT EXISTS word("
			+ "word_id INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ "name VARCHAR(100), "
			+ "language_code VARCHAR(2), "
			+ "FOREIGN KEY(language_code) REFERENCES language(language_code) ON UPDATE CASCADE"
			+ ")";
		final String	connString	= ConnectionDetails.getInstance().getConnectionString();
		try (final Connection conn = DriverManager.getConnection(connString); final Statement stmt = conn.createStatement())
		{
			stmt.execute(query);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		LogItem.exit();
	}
	
	public static final void clearCache()
	{
		LogItem.enter();
		Word.cache.clear();
		LogItem.exit();
	}
	
	public static final Word get(final Integer word_id)
	{
		LogItem.enter();
		if (Word.cache.containsKey(word_id))
		{
			Word w = Word.cache.get(word_id);
			LogItem.exit();
			return w;
		}
		Word w = Word.readEntity(word_id);
		LogItem.exit();
		return w;
	}
	
	public static final Word get(final String name, final Language l)
	{
		LogItem.enter();
		Word w = Word.readEntity(name, l.getLanguage_code());
		LogItem.exit();
		return w;
	}
	
	public static final Word createWord(final String name, final Language l)
	{
		LogItem.enter();
		Word w = Word.get(name, l);
		if (w == null)
		{
			w = new Word(-1, name, l);
			final Integer word_id = Word.writeEntity(w);
			w.setWord_id(word_id);
			Word.cache.put(word_id, w);
		}
		LogItem.exit();
		return w;
	}
	
	public static final void removeWord(final Integer word_id)
	{
		LogItem.enter();
		Word.cache.remove(word_id);
		final String	query		= "DELETE FROM word "
			+ "WHERE word_id = ?";
		final String	connString	= ConnectionDetails.getInstance().getConnectionString();
		try (final Connection conn = DriverManager.getConnection(connString); final PreparedStatement pstmt = conn.prepareStatement(query))
		{
			pstmt.setInt(1, word_id);
			pstmt.execute();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		LogItem.exit();
	}
	
	public static final void removeWord(final String name, final Language l)
	{
		LogItem.enter();
		Word.cache.remove(Word.get(name, l).getWord_id());
		final String	query		= "DELETE FROM word "
			+ "WHERE word_id = ?";
		final String	connString	= ConnectionDetails.getInstance().getConnectionString();
		try (final Connection conn = DriverManager.getConnection(connString);
			final PreparedStatement pstmt = conn.prepareStatement(query))
		{
			pstmt.setInt(1, Word.get(name, l).getWord_id());
			pstmt.executeUpdate();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		LogItem.exit();
	}
	
	public static final void removeAllWords()
	{
		LogItem.enter();
		Word.clearCache();
		ConnectionDetails.getInstance().executeSimpleStatement("DELETE FROM word");
		LogItem.exit();
	}
	
	private static final Word readEntity(Integer word_id)
	{
		LogItem.enter();
		final String	query		= "SELECT * "
			+ "FROM word "
			+ "WHERE word_id = ?";
		final String	connString	= ConnectionDetails.getInstance().getConnectionString();
		try (final Connection conn = DriverManager.getConnection(connString);
			final PreparedStatement pstmt = conn.prepareStatement(query))
		{
			pstmt.setInt(1, word_id);
			final ResultSet rs = pstmt.executeQuery();
			if (rs.next())
			{
				final String	name			= rs.getString("name");
				final String	language_code	= rs.getString("language_code");
				final Language	l				= Language.get(language_code);
				final Word		w				= new Word(word_id, name, l);
				Word.cache.put(word_id, w);
				rs.close();
				LogItem.exit();
				return w;
			}
			rs.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		LogItem.exit();
		return null;
	}
	
	private static final Word readEntity(final String name, final String language_code)
	{
		LogItem.enter();
		final String	query		= "SELECT * "
			+ "FROM word "
			+ "WHERE name = ? "
			+ "AND language_code = ?";
		final String	connString	= ConnectionDetails.getInstance().getConnectionString();
		try (final Connection conn = DriverManager.getConnection(connString);
			final PreparedStatement pstmt = conn.prepareStatement(query))
		{
			pstmt.setString(1, name);
			pstmt.setString(2, language_code);
			final ResultSet rs = pstmt.executeQuery();
			if (rs.next())
			{
				final Integer	word_id	= rs.getInt("word_id");
				final Language	l		= Language.get(language_code);
				final Word		w		= new Word(word_id, name, l);
				Word.cache.put(word_id, w);
				rs.close();
				LogItem.exit();
				return w;
			}
			rs.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		LogItem.exit();
		return null;
	}
	
	private static final Integer writeEntity(final Word word)
	{
		LogItem.enter();
		final String	query		= "INSERT INTO word(name, language_code) "
			+ "VALUES(?, ?)";
		final String	connString	= ConnectionDetails.getInstance().getConnectionString();
		try (final Connection conn = DriverManager.getConnection(connString);
			final PreparedStatement pstmt = conn.prepareStatement(query))
		{
			pstmt.setString(1, word.getName());
			pstmt.setString(2, word.getLanguage().getLanguage_code());
			pstmt.executeUpdate();
			final ResultSet rs = pstmt.getGeneratedKeys();
			rs.next();
			final Integer word_id = rs.getInt(1);
			LogItem.exit();
			return word_id;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		LogItem.exit();
		return -1;
	}
	
	public final Integer getWord_id()
	{
		LogItem.enter();
		LogItem.exit();
		return this.word_id;
	}
	
	private final void setWord_id(final Integer word_id)
	{
		LogItem.enter();
		this.word_id = word_id;
		LogItem.exit();
	}
	
	public final String getName()
	{
		LogItem.enter();
		LogItem.exit();
		return this.name;
	}
	
	public final void setName(final String name)
	{
		LogItem.enter();
		final String	query		= "UPDATE word "
			+ "SET name = ? "
			+ "WHERE word_id = ?";
		final String	connString	= ConnectionDetails.getInstance().getConnectionString();
		try (final Connection conn = DriverManager.getConnection(connString);
			final PreparedStatement pstmt = conn.prepareStatement(query))
		{
			pstmt.setString(1, name);
			pstmt.setInt(2, this.word_id);
			pstmt.executeUpdate();
			this.name = name;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		LogItem.exit();
	}
	
	public final Language getLanguage()
	{
		LogItem.enter();
		LogItem.exit();
		return this.language;
	}
	
	public final void setLanguage(final Language l)
	{
		LogItem.enter();
		final String	query		= "UPDATE word "
			+ "SET language_code = ? "
			+ "WHERE word_id = ?";
		final String	connString	= ConnectionDetails.getInstance().getConnectionString();
		try (final Connection conn = DriverManager.getConnection(connString);
			final PreparedStatement pstmt = conn.prepareStatement(query))
		{
			pstmt.setString(1, l.getLanguage_code());
			pstmt.setInt(2, this.word_id);
			pstmt.executeUpdate();
			this.language = l;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		LogItem.exit();
	}
	
	public final List<Translation> getTranslations(final Language l)
	{
		LogItem.enter();
		final List<Translation>	translations	= new ArrayList<>();
		final String			query			= "SELECT word1_id, word2_id "
			+ "FROM translation t "
			+ "JOIN word w2 ON t.word2_id = w2.word_id "
			+ "WHERE t.word1_id = ? "
			+ "AND w2.language_code = ? "
			+ "UNION "
			+ "SELECT word1_id, word2_id "
			+ "FROM translation t "
			+ "JOIN word w1 ON t.word1_id = w1.word_id "
			+ "WHERE t.word2_id = ? "
			+ "AND w1.language_code = ?";
		final String			connString		= ConnectionDetails.getInstance().getConnectionString();
		try (final Connection conn = DriverManager.getConnection(connString);
			final PreparedStatement pstmt = conn.prepareStatement(query))
		{
			pstmt.setInt(1, this.word_id);
			pstmt.setString(2, l.getLanguage_code());
			pstmt.setInt(3, this.word_id);
			pstmt.setString(4, l.getLanguage_code());
			final ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				final Integer		word1_id	= rs.getInt("word1_id");
				final Integer		word2_id	= rs.getInt("word2_id");
				final Word			w1			= Word.get(word1_id);
				final Word			w2			= Word.get(word2_id);
				final Translation	t			= Translation.get(w1, w2);
				translations.add(t);
			}
			rs.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		LogItem.exit();
		return translations;
	}
	
	public final List<WordCheck> getWordChecks(final Language l)
	{
		LogItem.enter();
		final List<WordCheck>	wordchecks	= new ArrayList<>();
		final String			query		= "SELECT c.trial_id "
			+ "FROM wordcheck c "
			+ "JOIN trial t ON c.trial_id = t.trial_id "
			+ "WHERE c.word_id = ? "
			+ "AND t.language_code_to = ?";
		final String			connString	= ConnectionDetails.getInstance().getConnectionString();
		try (final Connection conn = DriverManager.getConnection(connString); final PreparedStatement pstmt = conn.prepareStatement(query))
		{
			pstmt.setInt(1, this.word_id);
			pstmt.setString(2, l.getLanguage_code());
			final ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				final Integer	trial_id	= rs.getInt("trial_id");
				final WordCheck	c			= WordCheck.get(this, Trial.get(trial_id));
				wordchecks.add(c);
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		LogItem.exit();
		return wordchecks;
	}
	
}
