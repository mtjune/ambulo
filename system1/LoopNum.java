package jp.kagawanct.shigeta2013.ambulo1;

import android.util.Log;

public class LoopNum {
	final static int NUMDATA = 100;
	private int num; //numは一番新しいデータが入っている場所
	private int[] dataArray;
	
	public LoopNum(){ //コンストラクタ
		Log.d("loopnum()", "check 1");
		num = 0;
		Log.d("loopnum()", "check 2");
		dataArray = new int[NUMDATA];
		Log.d("loopnum()", "check Perfect");
	}
	
	private void incnum(){ //numを1増加
		if(num != (NUMDATA - 1))
			num++;
		else if(num == (NUMDATA - 1))
			num = 0;    		
	}
	private int verdecnum(int minus){ //minus分だけnumを減少した値を返す
		int ver = num;
		for( ; minus<0; minus--){
    		if(ver != 0)
    			ver--;
    		else if(ver == 0)
    			ver = NUMDATA-1;
		}
		return ver;
	}
	public int popdata(int minus){ //minus前のdataを取り出す
		return dataArray[verdecnum(minus)];
	}
	public void pushdata(int in){ //numを1増加させてそこにデータを入れる
		incnum();
		Log.d("pushdata", "check 1");
		dataArray[num] = in;
		Log.d("pushdata", "check 2");
	}
	public int getnum(){ //numをそのまま返す
		return num;
	}
}
