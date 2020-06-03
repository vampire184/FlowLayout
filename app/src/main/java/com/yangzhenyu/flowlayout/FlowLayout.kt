package com.yangzhenyu.flowlayout

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.core.view.forEachIndexed
import kotlin.math.max

class FlowLayout : ViewGroup{

    private val TAG = "FlowLayout"
    private var hPadding: Int = dp2px(8)
    private var vPadding: Int = dp2px(5)
    private var mAllViews: ArrayList<ArrayList<View>> = ArrayList()
    private var mLineViews: ArrayList<View> = ArrayList()
    private var mLineHeight: ArrayList<Int> = ArrayList()


    constructor(context: Context):this(context,null)

    constructor(context: Context,attrs: AttributeSet?): this(context, attrs,0)

    constructor(context: Context,attrs: AttributeSet?,defStyleAttr: Int): super(context,attrs,defStyleAttr)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        mAllViews.clear()
        mLineViews.clear()
        mLineHeight.clear()

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val measureWidth = MeasureSpec.getSize(widthMeasureSpec)-paddingLeft-paddingRight
        val measureHeight = MeasureSpec.getSize(heightMeasureSpec)-paddingTop-paddingBottom

        //布局的真实尺寸
        var realWidth = 0
        var realHeight = 0

        //每行的可用宽度，默认等于测量宽度
        var remainWidth = measureWidth

        //已经使用的尺寸
        var tempWidth = 0
        var tempHeight = 0

        forEachIndexed { index, view ->
            val childParams:MarginLayoutParams = view.layoutParams as MarginLayoutParams
            measureChildWithMargins(view,widthMeasureSpec,0,heightMeasureSpec,0)
            val childWidth = view.measuredWidth+childParams.leftMargin+childParams.rightMargin
            val childHeight = view.measuredHeight+childParams.topMargin+childParams.bottomMargin
            if (index==childCount-1){
                Log.d(TAG, "孩子宽度：$childWidth 高度：$childHeight")
            }

            remainWidth -= (childWidth+hPadding)
            if (remainWidth<=0){
                //重置每行的可用宽度
                remainWidth = measureWidth-childWidth-hPadding

                //换行啦,换行之后，需要保存上一行的view和高度到集合中，然后重置临时数据
                mAllViews.add(mLineViews.clone() as java.util.ArrayList<View>)
                mLineViews.clear()
                mLineViews.add(view)

                realWidth = max(realWidth,tempWidth)
                tempWidth = childWidth+hPadding

                realHeight += tempHeight
                mLineHeight.add(tempHeight)
                tempHeight = childHeight+vPadding

            }else{
                mLineViews.add(view)
                tempWidth +=(childWidth+hPadding)
                //这一行的最高的view的高度为该行的高度
                tempHeight = max(tempHeight,childHeight+vPadding)
            }
            if (index == childCount-1){
                //重置每行的可用宽度
                remainWidth = measureWidth-childWidth-hPadding


                //换行啦,换行之后，需要保存上一行的view和高度到集合中，然后重置临时数据
                mAllViews.add(mLineViews.clone() as java.util.ArrayList<View>)
                mLineViews.clear()

                realWidth = max(realWidth,tempWidth)

                realHeight += tempHeight
                mLineHeight.add(tempHeight)
                tempHeight = 0
            }
        }

        realWidth = if (widthMode == MeasureSpec.EXACTLY) measureWidth else realWidth
        realHeight = if (heightMode == MeasureSpec.EXACTLY) measureHeight else realHeight
        setMeasuredDimension(realWidth+paddingLeft+paddingRight,realHeight+paddingTop+paddingBottom)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var left = paddingLeft
        var top = paddingTop

        mAllViews.forEachIndexed { index,list ->

            list.forEach {
                val childParams:MarginLayoutParams = it.layoutParams as MarginLayoutParams
                val cl = left+childParams.leftMargin
                val cT = top+childParams.topMargin
                val cR = cl+it.measuredWidth
                val cB = cT+it.measuredHeight

                it.layout(cl,cT,cR,cB)
                left +=(it.measuredWidth+hPadding)
            }
            left = paddingLeft
            top +=(mLineHeight[index])
        }
    }

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
        return MarginLayoutParams(context,attrs)
    }

    private fun dp2px(value: Int): Int{
        val scale = context.resources.displayMetrics.density
        return (scale*value + 0.5f).toInt()
    }

}