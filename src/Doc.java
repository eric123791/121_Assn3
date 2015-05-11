import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Doc {
	private String text;
	private String _id;
	private String title;
	@JsonIgnore
	private String html;
	private String id;

	public String getText()
	{
		return text;
	}

	public void setText(String text)
	{
		this.text = text;
	}

	public String get_id()
	{
		return _id;
	}

	public void set_id(String _id)
	{
		this._id = _id;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getHtml()
	{
		return html;
	}

	public void setHtml(String html)
	{
		this.html = html;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(text);
		sb.append(_id);
		sb.append(title);
		sb.append(id);
		return sb.toString();
	}
}
