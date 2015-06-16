package jp.kagawanct.shigeta2013.familynotice;

import jp.kagawanct.shigeta2013.familynotice.R;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceFragment;

public class pSetingActivity extends Activity {

	@Override
	protected void onCreate(Bundle saveInstanceState) {
		super.onCreate(saveInstanceState);

		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, new PrefsFragment()).commit();

	}

	//フラグメントのスタティックな内部クラス
	public static class PrefsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {

		private static String USERNAME_KEY;
		private static String SERIALNUM_KEY;
		private static String TELNUM_KEY;

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.setingpref);
			USERNAME_KEY = getString(R.string.UserName_key);
			SERIALNUM_KEY = getString(R.string.SirialNum_key);
			TELNUM_KEY = getString(R.string.TelNum_key);

			//アクティビティに入った時にサマリーを変更
			EditTextPreference mUserName = (EditTextPreference) findPreference(USERNAME_KEY);
			mUserName.setSummary(mUserName.getText());
			EditTextPreference mSirialNum = (EditTextPreference) findPreference(SERIALNUM_KEY);
			mSirialNum.setSummary(mSirialNum.getText());
			EditTextPreference mTelNum = (EditTextPreference) findPreference(TELNUM_KEY);
			mTelNum.setSummary(mTelNum.getText());

		}

		//プレファレンス書き換えに反応してサマリーを変更
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

			if (key.equals(USERNAME_KEY)) {
				EditTextPreference mUserName = (EditTextPreference) findPreference(USERNAME_KEY);
				mUserName.setSummary(mUserName.getText());
			}
			if (key.equals(SERIALNUM_KEY)) {
				EditTextPreference mSirialNum = (EditTextPreference) findPreference(SERIALNUM_KEY);
				mSirialNum.setSummary(mSirialNum.getText());
			}
			if (key.equals(TELNUM_KEY)) {
				EditTextPreference mTelNum = (EditTextPreference) findPreference(TELNUM_KEY);
				mTelNum.setSummary(mTelNum.getText());
			}

		}

		@Override
		public void onResume() {
			super.onResume();
			getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		}

		@Override
		public void onPause() {
			super.onPause();
			getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		}

	}

}
