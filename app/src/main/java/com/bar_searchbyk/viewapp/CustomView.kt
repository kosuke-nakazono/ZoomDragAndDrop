package com.bar_searchbyk.viewapp
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.sqrt

class CustomView: View {
    // カスタムview作成用コンストラクタ
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int,defStyleRes: Int) : super(context, attrs, defStyleAttr,defStyleRes)

    // ユーザーが進行中のアクションを管理
    private var mMode = MODE.NONE
    // 二つの指の間の距離を管理
    private var mOldDistance = 0f
    // ズームの倍率(スケール)を管理
    private var mScale = 10f
    // 円のx座標とy座標を管理
    private var mXPosition = 0f
    private var mYPosition = 0f
    // タッチされた座標を管理
    private var mTouchPosition = Point()
    // 円の色はこのPaintクラスで指定する
    private val mPaint = Paint().apply {
        color = Color.BLACK
    }

    // 初期表示位置は親Viewの中心にセットする
    fun initView(width: Float,height: Float){
        mXPosition = width / 2
        mYPosition = height / 2
    }

    // Viewが最描画される度に呼ばれる
    override fun onDraw(canvas: Canvas?) {
        if(canvas == null){
            super.onDraw(canvas)
            return
        }
        // drawCircle()前にcanvasの状態を保存する
        canvas.save()
        // 円を描画する
        canvas.drawCircle(mXPosition,mYPosition,mScale * SCALE_GRADE,mPaint)
        // 円の描画後、canvas.save()時のcanvasの状態に戻し、drawCircle()の描画開始時のcanvasを統一する
        canvas.restore()
        super.onDraw(canvas)
    }

    // タッチされた座標が円上の座標かどうかをチェックする
    private fun isTouchedPointOnCircle(eX: Float, eY: Float):Boolean{
        // 余裕を持って半径の1.2倍以内であれば円上として判断する
        val radius = mScale * SCALE_GRADE * 1.2f
       return eX > mXPosition - radius && eX < mXPosition + radius
               && eY > mYPosition - radius && eY < mYPosition + radius
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?:return false
        when(event.actionMasked){
            MotionEvent.ACTION_DOWN -> {
                // タッチされた座標が円上でなければこれ以降はスルー
                if(!isTouchedPointOnCircle(event.x,event.y)) return true
                // ドラッグ開始を記録
                mMode = MODE.DRAG
                // タッチされたx座標及びy座標を保存
                mTouchPosition.set(event.x.toInt(), event.y.toInt())
                // falseを指定するとこれ以降のタッチイベントが呼ばれなくなる
                return true
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                //  2本の指の内1本でも円上をタッチしていればスケールする
                if(!(isTouchedPointOnCircle(event.getX(0),event.getY(0)) || isTouchedPointOnCircle(event.getX(1),event.getY(1)))) return false
                // ズーム開始を記録
                mMode = MODE.ZOOM
                // タップされた2本の指の間の距離を記録
                mOldDistance = spacing(event)
                // falseを指定するとこれ以降のタッチイベントが呼ばれなくなる
                return true
            }
            // シングルタッチ、マルチタッチ双方で呼ばれるため、タッチ開始時のモードで場合わけ
            MotionEvent.ACTION_MOVE -> {
                if (mMode == MODE.DRAG) {
                    translate(event)
                } else if (mMode == MODE.ZOOM) {
                    scale(event)
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (mMode != MODE.DRAG) return false
                // 進行中アクションを終了とする
                mMode = MODE.NONE
                translate(event)
                return false
            }
            MotionEvent.ACTION_POINTER_UP -> {
                if (mMode != MODE.ZOOM) return false
                // 進行中アクションを終了とする
                mMode = MODE.NONE
                scale(event)
                return false
            }
            else -> {
                return true
            }
        }
    }

    // スケールを更新
    private fun scale(event: MotionEvent) {
        // 2本の指の間の距離の変化率からスケール率を取得
        val newDest = spacing(event)
        val newScale = mScale * (newDest.div(mOldDistance))
        // 最大倍率以上にスケールさせない
        mScale =  if(newScale > MAX_SCALE){
            MAX_SCALE
        }else{
            newScale
        }
        mOldDistance = spacing(event)
        invalidate()
    }

    private fun translate(event: MotionEvent){
        // (スクロール後のタッチ座標 - スクロール前のタッチ座標) + 更新前の円の座標 → 更新された円の座標
        mXPosition += (event.x - mTouchPosition.x)
        mYPosition += (event.y - mTouchPosition.y)
        // スクロール後の座標を保存
        mTouchPosition.set(event.x.toInt(), event.y.toInt())
        // ここで更新された座標で円を最描画する
        invalidate()
    }

    // タップされた2本の指の間の距離を算出する
    private fun spacing(event: MotionEvent): Float {
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return sqrt(x * x + y * y)
    }

    companion object{
        enum class MODE(val type:Int){
            // アクション未実行時
            NONE(0),
            // ドラッグ中
            DRAG(1),
            // ズーム中
            ZOOM(2),
        }
        // 最大倍率
        private const val MAX_SCALE = 50f
        // 倍率係数
        private const val SCALE_GRADE = 10f
    }
}