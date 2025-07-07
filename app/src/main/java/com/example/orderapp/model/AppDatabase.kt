package com.example.orderapp.model

import androidx.core.content.edit

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import android.content.SharedPreferences

@Database(entities = [Product::class, OrderHistory::class, OrderHistoryLine::class, Category::class], version = 7, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun orderHistoryDao(): OrderHistoryDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        private const val PREFS_NAME = "OrderAppPrefs"
        private const val KEY_INITIAL_DATA_INSERTED = "initial_data_inserted"

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                @Suppress("DEPRECATION")
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "order_app_database"
                )
                    .fallbackToDestructiveMigrationFrom(1, 2, 3, 4, 5, 6)
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                            val initialDataInserted = prefs.getBoolean(KEY_INITIAL_DATA_INSERTED, false)

                            if (!initialDataInserted) {
                                CoroutineScope(Dispatchers.IO).launch {
                                    val database = getDatabase(context)
                                    val productDao = database.productDao()
                                    val categoryDao = database.categoryDao()

                                    val initialDataJson = """
                                        {"products":[{"id":2,"name":"オールスパイスパウダー","price":1615,"unit":"g","amount":500,"categoryId":1},{"id":3,"name":"ドライマンゴーパウダー（アムチュール）","price":640,"unit":"g","amount":500,"categoryId":1,"order":1},{"id":4,"name":"ブラックペッパーパウダー","price":1350,"unit":"g","amount":500,"categoryId":1,"order":2},{"id":5,"name":"ブラックペッパーホール","price":1180,"unit":"g","amount":500,"categoryId":1,"order":3},{"id":6,"name":"ブラウンカルダモン","price":3600,"unit":"g","amount":500,"categoryId":1,"order":4},{"id":7,"name":"グリーンカルダモンパウダー","price":4100,"unit":"g","amount":500,"categoryId":1,"order":5},{"id":8,"name":"グリーンカルダモンホール","price":4000,"unit":"g","amount":500,"categoryId":1,"order":6},{"id":31,"name":"キャラウェイ","price":1265,"unit":"g","amount":500,"categoryId":1,"order":7},{"id":10,"name":"チリパウダーホット","price":1070,"unit":"g","amount":1000,"categoryId":1,"order":8},{"id":11,"name":"シナモンパウダー","price":539,"unit":"g","amount":500,"categoryId":1,"order":9},{"id":12,"name":"シナモンスティック","price":262,"unit":"g","amount":250,"categoryId":1,"order":10},{"id":13,"name":"クローブパウダー","price":1595,"unit":"g","amount":500,"categoryId":1,"order":11},{"id":14,"name":"クローブホール","price":1420,"unit":"g","amount":500,"categoryId":1,"order":12},{"id":15,"name":"コリアンダーパウダー","price":750,"unit":"g","amount":1000,"categoryId":1,"order":13},{"id":16,"name":"コリアンダーホール","price":374,"unit":"g","amount":500,"categoryId":1,"order":14},{"id":17,"name":"クミンパウダー","price":1190,"unit":"g","amount":1000,"categoryId":1,"order":15},{"id":18,"name":"クミンホール","price":1100,"unit":"g","amount":1000,"categoryId":1,"order":16},{"id":19,"name":"フェンネルラクナビ","price":630,"unit":"g","amount":500,"categoryId":1,"order":17},{"id":20,"name":"フェンネルシード","price":430,"unit":"g","amount":500,"categoryId":1,"order":18},{"id":21,"name":"フェヌグリークパウダー","price":550,"unit":"g","amount":500,"categoryId":1,"order":19},{"id":22,"name":"フェヌグリークホール","price":385,"unit":"g","amount":500,"categoryId":1,"order":20},{"id":23,"name":"ガーリックパウダー","price":550,"unit":"g","amount":500,"categoryId":1,"order":21},{"id":24,"name":"ジンジャーパウダー","price":550,"unit":"g","amount":500,"categoryId":1,"order":22},{"id":25,"name":"ヒングパウダー","price":313,"unit":"g","amount":50,"categoryId":1,"order":23},{"id":26,"name":"カロンジ（ニゲラ）","price":550,"unit":"g","amount":500,"categoryId":1,"order":24},{"id":27,"name":"マスタードシードブラウン","price":308,"unit":"g","amount":500,"categoryId":1,"order":25},{"id":28,"name":"パプリカパウダー（スペイン）","price":1090,"unit":"g","amount":1000,"categoryId":1,"order":26},{"id":29,"name":"ターメリックパウダー","price":900,"unit":"g","amount":1000,"categoryId":1,"order":27},{"id":30,"name":"チムールホール（山椒）","price":1100,"unit":"g","amount":200,"categoryId":1,"order":28},{"id":32,"name":"バジルリーフ","price":935,"unit":"g","amount":500,"categoryId":2,"order":29},{"id":33,"name":"ベイリーフ","price":303,"unit":"g","amount":250,"categoryId":2,"order":30},{"id":34,"name":"カレーリーフ","price":176,"unit":"g","amount":100,"categoryId":2,"order":31},{"id":35,"name":"カスリメティ","price":860,"unit":"g","amount":500,"categoryId":2,"order":32},{"id":36,"name":"ペパーミントリーフ","price":220,"unit":"g","amount":100,"categoryId":2,"order":33},{"id":37,"name":"ティーマサラ","price":660,"unit":"g","amount":100,"categoryId":3,"order":34},{"id":38,"name":"レッドレンティル（マスールダルレッド）","price":330,"unit":"g","amount":1000,"categoryId":4,"order":35},{"id":39,"name":"ココナッツファイン","price":424,"unit":"g","amount":500,"categoryId":5,"order":36},{"id":40,"name":"バスマティライス（インディアゲート）","price":660,"unit":"g","amount":1000,"categoryId":6,"order":37},{"id":41,"name":"バスマティライス（パキスタン）","price":649,"unit":"g","amount":1000,"categoryId":6,"order":38},{"id":42,"name":"バスマティライス（コヒヌール）","price":740,"unit":"g","amount":1000,"categoryId":6,"order":39},{"id":43,"name":"グンドゥルック","price":726,"unit":"g","amount":200,"categoryId":9,"order":40},{"id":44,"name":"CTCティー（アッサムティー）","price":495,"unit":"g","amount":500,"categoryId":10,"order":41},{"id":45,"name":"ローカルギー","price":1540,"unit":"g","amount":900,"categoryId":11,"order":42},{"id":46,"name":"ホット＆スパイシーラプシーピクルス","price":650,"unit":"g","amount":400,"categoryId":12,"order":43},{"id":47,"name":"ピクルス グリーンチリ","price":590,"unit":"g","amount":300,"categoryId":12,"order":44}],"categories":[{"id":1,"name":"スパイス"},{"id":2,"name":"ハーブ","order":1},{"id":3,"name":"ミックススパイス","order":2},{"id":4,"name":"豆","order":3},{"id":5,"name":"ココナッツ製品","order":4},{"id":7,"name":"ドライフルーツ","order":5},{"id":8,"name":"オニオン","order":6},{"id":6,"name":"米製品","order":7},{"id":9,"name":"その他商品","order":8},{"id":10,"name":"紅茶","order":9},{"id":11,"name":"オイル","order":10},{"id":12,"name":"ピクルス","order":12}]}
                                    """.trimIndent()

                                    val gson = Gson()
                                    val type = object : TypeToken<InitialData>() {}.type
                                    val initialData: InitialData = gson.fromJson(initialDataJson, type)

                                    initialData.categories.forEach { category ->
                                        categoryDao.insertCategory(category)
                                    }
                                    initialData.products.forEach { product ->
                                        productDao.insertProduct(product)
                                    }

                                    prefs.edit { putBoolean(KEY_INITIAL_DATA_INSERTED, true) }
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

data class InitialData(
    val products: List<Product>,
    val categories: List<Category>
)