package com.example.orderapp.model

data class Changelog(val version: String, val changes: List<String>)

fun getChangelog(): List<Changelog> {
    return listOf(
        Changelog("1.09", listOf("設定画面のスクロールに対応", "CSVインポートでCSVファイルが選択できるように修正")),
        Changelog("1.08", listOf("データ管理機能の強化（CSV/JSONエクスポート・インポート）")),
        Changelog("1.07", listOf("発注ボタンを押した際に、まず発注履歴に保存し、その後で共有画面が表示されるように修正（共有画面を閉じると商品リストに戻る）", "共有ボタンを押さなくても発注履歴が保存されるように修正", "注文画面でカテゴリが1回のタップで展開されるように修正", "商品リストに検索・フィルタリング機能を折りたたみ可能に修正", "アプリのタイトルバーを完全に削除")),
        Changelog("1.05", listOf("注文画面でカテゴリが1回のタップで展開されるように修正")),
        Changelog("1.04", listOf("発注ボタンから注文内容をエクスポートする機能を追加")),
        Changelog("1.03", listOf("テーマ設定が次回起動時にリセットされる問題を修正")),
        Changelog("1.02", listOf("各種削除ボタンに確認ダイアログを追加")),
        Changelog("1.01", listOf("注文内容確認画面のレイアウトを修正", "合計金額や税率の表示をスクロールの一番下に移動"))
    )
}