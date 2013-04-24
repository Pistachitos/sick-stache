package org.sickstache.task;

import org.sickstache.helper.Preferences;

public class SubtitleSearchTask extends SickTask<Void,Void,Boolean> {
	
	private String tvdbid = null;
	private String season = null;
	private String episode = null;
	
	public SubtitleSearchTask( Preferences pref, String tvdbid, String season, String episode )
	{
		super(pref);
		this.tvdbid = tvdbid;
		this.season = season;
		this.episode = episode;
	}

	@Override
	public String getTaskLogName() {
		return "SubtitleSearchTask";
	}
	
	@Override
	protected Boolean doInBackground(Void... arg0) {
		try {
			return pref.getSickBeard().subtitleSearch(tvdbid, season, episode);
		} catch (Exception e) {
			error = e;
			return null;
		}
	}
}
