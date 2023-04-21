package com.zhanghao.player

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
import androidx.lifecycle.*

/****
 * 基于viewModel和liveData的权限请求
 * 自动管理生命周期
 *  created by zhanghao
 */
class ZPermission  : Fragment {
    private lateinit var activity: AppCompatActivity
    private val requestCode = 9990
    private var permissions: Array<out String>? = null
    var refusePermissions: ArrayList<String> = arrayListOf()
    var allowPermissions: ArrayList<String> = arrayListOf()
    private var allowListener: (() -> Unit)? = null
    private var refuseListener: ((list: ArrayList<String>?) -> Unit)? = null
    var allow = MutableLiveData<Boolean>()
    private val TAG = "ZPermission"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = View(context)
        view.setBackgroundColor(Color.TRANSPARENT)
        return view
    }

    private constructor()

    private constructor(activity: AppCompatActivity){
        this.activity=activity
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        allow.observe(this.activity, Observer {
            if (it) {
                allowListener?.invoke()
                activity.supportFragmentManager.beginTransaction().remove(this).commit()
            } else {
                refuseListener?.invoke(refusePermissions)
                activity.supportFragmentManager.beginTransaction().remove(this).commit()
            }
        })
        if (permissions!=null){
            requestPermissions(permissions!!,requestCode)
        }else{
            activity.supportFragmentManager.beginTransaction().remove(this).commit()
        }
    }

    companion object {
        fun get(activity: Activity): ZPermission? {
            var zPermission: ZPermission? = null
            if (activity is AppCompatActivity) {
                zPermission = ZPermission(activity)
                activity.supportFragmentManager.beginTransaction().add(zPermission,"ZPermissionF").commit()
            } else {
                Log.w("ZPermission", "警告：传入的Activity不是一个AppCompatActivity类，取消本次请求")
                return null
            }
            return zPermission
        }
    }

    /***
     * 请求权限
     */
    fun req(vararg permissions: String): ZPermission {
        this.permissions = permissions
        return this
    }

    fun listener(
        allow: (() -> Unit)? = null,
        refuse: ((list: ArrayList<String>?) -> Unit)? = null
    ) {
        allowListener = allow
        refuseListener = refuse
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == requestCode) {
            var isAllow = true
            for (i in permissions) {
                //如果授予权限
                if (PermissionChecker.checkSelfPermission(activity,i) == PermissionChecker.PERMISSION_GRANTED
                ) {
                    allowPermissions.add(i)
                } else {
                    isAllow=false
                    refusePermissions.add(i)
                }
            }
            allow.value = isAllow
        }
    }

    override fun onDestroyView() {
        Log.w(TAG, "onDestroyView")
        super.onDestroyView()
    }

}