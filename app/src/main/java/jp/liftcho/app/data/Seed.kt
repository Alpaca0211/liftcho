package jp.liftcho.app.data

/**
 * 初回起動時に入れる基準メニュー。
 * 利用者が自分のメニューに置き換えて使う前提の初期値。
 */
object Seed {

    val dayLabels: Map<String, String> = linkedMapOf(
        "mon" to "胸・三頭",
        "tue" to "背中・二頭",
        "thu" to "脚",
        "fri" to "肩",
        "sat" to "腕・腹"
    )

    private var order = 0

    private fun ex(
        id: String,
        name: String,
        day: String,
        sets: Int,
        weight: Double?,
        reps: Int,
        repMin: Int,
        repMax: Int,
        step: Double,
        rest: Int
    ) = Exercise(id, name, day, order++, sets, weight, reps, repMin, repMax, step, rest)

    val exercises: List<Exercise> = listOf(
        // 月 胸・三頭
        ex("bench", "ベンチプレス", "mon", 4, 60.0, 8, 8, 10, 2.5, 150),
        ex("incline-chest-press", "インクラインチェストプレス", "mon", 3, 40.0, 10, 10, 12, 2.5, 90),
        ex("chest-press", "チェストプレス", "mon", 3, 25.0, 12, 10, 12, 2.5, 90),
        ex("pec-fly", "ペックフライ", "mon", 3, 30.0, 12, 12, 15, 2.5, 75),
        ex("pushdown-rope", "プレスダウン（ロープ）", "mon", 3, 15.0, 15, 12, 15, 2.5, 60),
        ex("overhead-ext", "オーバーヘッドEXT", "mon", 3, 15.0, 15, 12, 15, 2.5, 60),
        // 火 背中・二頭
        ex("deadlift", "デッドリフト", "tue", 4, 100.0, 8, 8, 10, 2.5, 180),
        ex("barbell-row", "ベントオーバーロウ", "tue", 3, 60.0, 10, 8, 10, 2.5, 120),
        ex("lat-pulldown", "ラットプルダウン", "tue", 4, 60.0, 10, 8, 10, 2.5, 90),
        ex("seated-row", "シーテッドロウ", "tue", 3, 60.0, 10, 10, 12, 2.5, 90),
        ex("shrug", "シュラッグ", "tue", 3, 60.0, 15, 12, 15, 2.5, 60),
        ex("barbell-curl", "バーベルカール", "tue", 3, 30.0, 10, 8, 10, 2.5, 75),
        // 木 脚
        ex("squat", "スクワット", "thu", 4, 80.0, 8, 8, 10, 2.5, 180),
        ex("hack-squat", "ハックスクワット", "thu", 3, 60.0, 10, 10, 12, 5.0, 120),
        ex("leg-press", "レッグプレス", "thu", 3, 120.0, 12, 10, 12, 5.0, 120),
        ex("leg-curl", "レッグカール", "thu", 3, 40.0, 12, 10, 12, 2.5, 75),
        ex("leg-extension", "レッグエクステンション", "thu", 3, 45.0, 15, 12, 15, 2.5, 60),
        // 金 肩
        ex("shoulder-press", "ショルダープレス", "fri", 4, 60.0, 10, 8, 10, 2.5, 120),
        ex("lateral-raise", "サイドレイズ", "fri", 4, 10.0, 15, 12, 15, 1.0, 60),
        ex("cable-lateral", "ケーブルサイドレイズ", "fri", 3, 5.0, 15, 12, 15, 1.25, 60),
        ex("rear-delt", "リアデルト", "fri", 4, 30.0, 15, 12, 15, 2.5, 60),
        ex("face-pull", "フェイスプル", "fri", 3, 10.0, 15, 12, 15, 1.25, 60),
        // 土 腕・腹
        ex("preacher-curl", "プリチャーカール", "sat", 3, 20.0, 10, 8, 10, 2.5, 75),
        ex("hammer-curl", "ハンマーカール", "sat", 3, 12.0, 12, 10, 12, 1.0, 60),
        ex("pushdown-vbar", "プレスダウン（Vバー）", "sat", 3, 25.0, 12, 10, 12, 2.5, 60),
        ex("ab-roller", "アブローラー", "sat", 3, null, 15, 15, 20, 0.0, 60),
        ex("leg-raise", "レッグレイズ", "sat", 3, null, 15, 15, 20, 0.0, 60),
        ex("torso-rotation", "トーソローテーション", "sat", 3, 50.0, 15, 12, 15, 2.5, 60)
    )
}
