package jp.alhinc.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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

		File[] files = new File(args[0]).listFiles(); //レビュー後修正
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
			//エラー処理2-1の内容
			//rcdFilesをソートする
			Collections.sort(rcdFiles);

			// 売上ファイルが連番か確認
			for (int i = 0; i < rcdFiles.size() - 1; i++) {
				//⽐較する2つのファイル名の先頭から数字の8⽂字を切り出し、int型に変換します。
				int former = Integer.parseInt(rcdFiles.get(i).getName().substring(0, 8));
				int latter = Integer.parseInt(rcdFiles.get(i + 1).getName().substring(0, 8));

				//2つのファイル名の数字を⽐較して、差が1ではなかった場合
				if ((latter - former) != 1) {
					System.out.println("売上ファイル名が連番になっていません");
					return;
				}
			}

			//2-2の処理
			//1行ずつ中身を見ていく処理
			BufferedReader br = null;
			try {
				for (int i = 0; i < rcdFiles.size(); i++) {
					//行単位で見ていくため、BufferedReaderメソッドを使用する
					br = new BufferedReader(new FileReader(rcdFiles.get(i)));

					//パラメータの取得
					String branchCode = br.readLine(); // 1行目: 支店コード
					String saleAmounts = br.readLine(); // 2行目: 売上金額

					//売上金額をlong型に変換
					long fileSale = Long.parseLong(saleAmounts);

					// 既存の売上金額に新しく読み込んだ売上金額を加算
					Long saleAmount = branchSales.get(branchCode);

					//加算した売上⾦額をMapに追加します。
					branchSales.put(branchCode, saleAmount + fileSale);
				}
			} catch (IOException e) {
				System.out.println(UNKNOWN_ERROR);
				return;
			} finally {
				// ファイルを開いている場合
				if (br != null) {
					try {
						// ファイルを閉じる
						br.close();
					} catch (IOException e) {
						System.out.println(UNKNOWN_ERROR);
						return;
					}
				}
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
			//エラー処理1の内容
			if (!file.exists()) {
				//支店定義ファイルが存在しない場合、コンソールにエラーメッセージを表示します。
				System.out.println(FILE_NOT_EXIST);
				return false;
			}
			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);

			String line;

			// 一行ずつ読み込む
			while ((line = br.readLine()) != null) {
				// ※ここの読み込み処理を変更してください。(処理内容1-2)

				//split を使って、items[0] には支店コード、items[1] には支店名を格納
				String[] items = line.split(",");

				//エラー処理1の内容
				// 少なくとも支店コードと支店名2つの要素があるか確認
				//⽀店定義ファイルの仕様が満たされていない場合
				if (items.length != 2 || !items[0].matches("\\d{3}")) {
					System.out.println(FILE_INVALID_FORMAT);
					return false;
				}
				// 支店コードをキー、支店名を値としてbranchNamesMapに格納
				branchNames.put(items[0], items[1]);
				//支店コードをキー、初期値として0L（Long型の0）を値としてbranchSalesMapに格納
				branchSales.put(items[0], 0L); // 売上金額を初期化

				//}
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
				// ファイルに書き込む文字列を作成（支店コード・支店名・合計金額）
				bw.write(key + "," + branchNames.get(key) + "," + branchSales.get(key));
				//改行
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