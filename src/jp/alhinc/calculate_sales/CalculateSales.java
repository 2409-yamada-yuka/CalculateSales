package jp.alhinc.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculateSales {

	// 支店定義ファイル名
	private static final String FILE_NAME_BRANCH_LST = "branch.lst";

	// 支店別集計ファイル名
	private static final String FILE_NAME_BRANCH_OUT = "branch.out";

	// エラーメッセージ
	private static final String UNKNOWN_ERROR = "予期せぬエラーが発生しました";
	private static final String FILE_NOT_EXIST = "支店定義ファイルが存在しません";
	private static final String FILE_INVALID_FORMAT = "支店定義ファイルのフォーマットが不正です";

	/**
	 * メインメソッド
	 *
	 * @param コマンドライン引数
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		// 支店コードと支店名を保持するMap
		Map<String, String> branchNames = new HashMap<>();
		// 支店コードと売上金額を保持するMap
		Map<String, Long> branchSales = new HashMap<>();

		// 支店定義ファイル読み込み処理
		if (!readFile(args[0], FILE_NAME_BRANCH_LST, branchNames, branchSales)) {
			return;
		}

		// ※ここから集計処理を作成してください。(処理内容2-1、2-2)

		//2-1の処理(ファイルの取得・ファイル名が数字8桁.rcdの判定と検索・該当ファイルの情報を保持)
		//listFilesを使⽤してfilesという配列に、
		//指定したパスに存在する全てのファイル(または、ディレクトリ)の情報を格納します。
		File[] files = new File("C:/Users/user/Desktop/売り上げ集計課題").listFiles();
		// 売上ファイルの情報を格納するリスト
		List<File> rcdFiles = new ArrayList<>();

		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				// files[i].getName() でファイル名が取得できます。

				// ファイル名を取得
				String fileName = files[i].getName();

				// matchesを使用してファイル名が「数字8桁.rcd」なのか判定します。
				if (fileName.matches("\\d{8}\\.rcd")) {

					// 売上ファイルの条件に当てはまったものだけ、List(ArrayList) に追加します。
					rcdFiles.add(files[i]);
				}
			}

			//2-2の処理
			// 売上ファイルの中身を保持するリストを作成(1行ずつ中身を見ていく処理)
			List<long[]> salesDataList = new ArrayList<>();

			for (int i = 0; i < rcdFiles.size(); i++) {
				//行単位で見ていくため、BufferedReaderメソッドを使用する
				BufferedReader br = new BufferedReader(new FileReader(rcdFiles.get(i)));

				//パラメータの取得
				String branchCode = br.readLine(); // 1行目: 支店コード
				String saleAmounts = br.readLine(); // 2行目: 売上金額

				//読み込み処理を閉じる
				br.close();

				//売上金額をlong型に変換
				long fileSale = Long.parseLong(saleAmounts);
				// 新しいListに追加
				salesDataList.add(new long[] { Long.parseLong(branchCode), fileSale });

				//読み込んだ売上⾦額を加算します。
				//Long saleAmount = 売上⾦額を⼊れたMap.get(⽀店コード) + longに変換した売上⾦額;
				// 既存の売上金額に新しく読み込んだ売上金額を加算
				Long saleAmount = branchSales.get(branchCode);

				//Mapに支店コードが存在しない場合、saleAmountを0L（long型の0）に設定
				if (saleAmount == null) {
					saleAmount = 0L;
				}
				//加算した売上⾦額をMapに追加します。
				branchSales.put(branchCode, saleAmount + fileSale);
			}
		}

		// 支店別集計ファイル書き込み処理
		if (!writeFile(args[0], FILE_NAME_BRANCH_OUT, branchNames, branchSales)) {
			return;
		}

	}

	/**
	 * 支店定義ファイル読み込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 読み込み可否
	 */
	private static boolean readFile(String path, String fileName, Map<String, String> branchNames,
			Map<String, Long> branchSales) {
		BufferedReader br = null;

		try {
			File file = new File(path, fileName);
			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);

			String line;

			// 一行ずつ読み込む
			while ((line = br.readLine()) != null) {
				//元の処理
				//System.out.println(line);

				// ※ここの読み込み処理を変更してください。(処理内容1-2)

				//split を使って「,」(カンマ)で分割すると、
				//items[0] には支店コード、items[1] には支店名が格納されます。
				String[] items = line.split(",");
				// 少なくとも支店コードと支店名2つの要素があるか確認
				if (items.length >= 2) {
					String branchCode = items[0];
					String branchName = items[1];
					// 支店コードをキー、支店名を値としてbranchNamesMapに格納
					branchNames.put(branchCode, branchName);
					//支店コードをキー、初期値として0L（Long型の0）を値としてbranchSalesMapに格納
					branchSales.put(branchCode, 0L); // 売上金額を初期化

				}
			}
		} catch (IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;
		} finally {
			// ファイルを開いている場合
			if (br != null) {
				try {
					// ファイルを閉じる
					br.close();
				} catch (IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 支店別集計ファイル書き込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 書き込み可否
	 * @throws IOException
	 */
	private static boolean writeFile(String path, String fileName, Map<String, String> branchNames,
			Map<String, Long> branchSales) throws IOException {
		// ※ここに書き込み処理を作成してください。(処理内容3-1)
		BufferedWriter bw = null;
		//
		try {
			bw = new BufferedWriter(new FileWriter(path + File.separator + fileName));

			// Mapから全てのKey(支店コード)を取得
			for (String key : branchNames.keySet()) {
				//keyという変数には、Mapから取得したキーが代入されています。
				//拡張for⽂で繰り返されているので、1つ⽬のキーが取得できたら、
				//2つ⽬の取得...といったように、次々とkeyという変数に上書きされていきます。
				//支店コードと支店名を保持するMapのキー
				String branchNameKey = branchNames.get(key);
				//支店コードと売上金額を保持するMapのキー
				Long branchSalesKey = branchSales.get(key);

				// ファイルに書き込む文字列を作成（支店コード・支店名・合計金額）
				//"%03d,%s,%010d"の使い方
				//%03d：3桁固定のその数字の前に0をつける
				//%s：文字列そのまま出力
				//%010d：整数を10進法で出力
				String line = String.format("%03d,%s,%010d", Integer.parseInt(key), branchNameKey, branchSalesKey);

				bw.write(line);
				// 改行
				bw.newLine();
			}
			//ファイル書き込みでの例外処理
		} catch (IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;

		} finally {
			// ファイルを開いている場合
			if (bw != null) {
				try {
					// ファイルを閉じる
					bw.close();
				} catch (IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}
		return true;

	}
}