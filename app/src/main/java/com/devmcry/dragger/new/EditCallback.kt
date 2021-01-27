package com.devmcry.dragger.new

/**
 *  @author : DevMcryYu
 *  @date : 1/27/21 5:08 PM
 *  @description :
 */
interface EditCallback {
    fun onSelected(view: BaseEditView)
    fun onUnSelected(view: BaseEditView)
    fun onAction(view: BaseEditView, type: EditType)
}