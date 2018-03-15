package com.boom.ems.emsboom

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.TELEPHONY_SERVICE
import android.content.Intent
import android.telephony.TelephonyManager
import android.telephony.TelephonyManager.ACTION_PHONE_STATE_CHANGED
import android.util.Log
import android.view.KeyEvent
import com.android.internal.telephony.ITelephony
import io.realm.Realm
import android.view.KeyEvent.KEYCODE_HEADSETHOOK


/**
 * Created by nham.manh.tuyen on 14/03/2018.
 */

class IncomingCallReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_PHONE_STATE_CHANGED) {
            val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

            if (!isInWhiteList(incomingNumber)) {
                Log.e("killCall", "killCall " + incomingNumber)
                killCall(context)
            } else {
                Log.e("answerPhoneAidl", "answerPhoneAidl " + incomingNumber)
                try {
                    answerPhoneAidl(context)
                } catch (e: Exception) {
                    e.printStackTrace()
                    answerPhoneHeadsethook(context)
                }
            }
        }
    }

    private fun isInWhiteList(phoneNumber: String): Boolean {
        try {
            val realm = Realm.getDefaultInstance()
            realm.beginTransaction()
            val allContact = realm.where(EMSContact::class.java)
                    .equalTo("phoneNumber", phoneNumber)
                    .equalTo("active", true)
                    .count()
            realm.commitTransaction()

            return allContact > 0
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return false
    }

    fun killCall(context: Context): Boolean {
        try {
            // Get the boring old TelephonyManager
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

            // Get the getITelephony() method
            val classTelephony = Class.forName(telephonyManager.javaClass.name)
            val methodGetITelephony = classTelephony.getDeclaredMethod("getITelephony")

            // Ignore that the method is supposed to be private
            methodGetITelephony.isAccessible = true

            // Invoke getITelephony() to get the ITelephony interface
            val telephonyInterface = methodGetITelephony.invoke(telephonyManager)

            // Get the endCall method from ITelephony
            val telephonyInterfaceClass = Class.forName(telephonyInterface.javaClass.name)
            val methodEndCall = telephonyInterfaceClass.getDeclaredMethod("endCall")

            // Invoke endCall()
            methodEndCall.invoke(telephonyInterface)

        } catch (ex: Exception) { // Many things can go wrong with reflection calls
            Log.d("IncomingCallReceiver", "PhoneStateReceiver **" + ex.toString())
            return false
        }

        return true
    }

//    private fun answerPhoneAidl(context: Context) {
//        try {
//            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
//            val classTelephony = Class.forName(telephonyManager.javaClass.name)
//            val methodGetITelephony = classTelephony.getDeclaredMethod("getITelephony")
//
//            methodGetITelephony.isAccessible = true
//
//            val telephonyInterface = methodGetITelephony.invoke(telephonyManager)
//
//            val telephonyInterfaceClass = Class.forName(telephonyInterface.javaClass.name)
//            val methodSilenceRinger = telephonyInterfaceClass.getDeclaredMethod("silenceRinger")
//            val methodAnswerRingingCall = telephonyInterfaceClass.getDeclaredMethod("answerRingingCall")
//
//            methodSilenceRinger.invoke(telephonyInterface)
//            methodAnswerRingingCall.invoke(telephonyInterface)
//
//        } catch (ex: Exception) { // Many things can go wrong with reflection calls
//            ex.printStackTrace()
//        }
//    }

    @Throws(Exception::class)
    private fun answerPhoneAidl(context: Context) {
        // Set up communication with the telephony service (thanks to Tedd's Droid Tools!)
        val tm = context.getSystemService(TELEPHONY_SERVICE) as TelephonyManager?
        val c = Class.forName(tm!!.javaClass.name)
        val m = c.getDeclaredMethod("getITelephony")
        m.isAccessible = true
        val telephonyService: ITelephony
        telephonyService = m.invoke(tm) as ITelephony

        // Silence the ringer and answer the call!
        telephonyService.silenceRinger()
        telephonyService.answerRingingCall()
    }

    private fun answerPhoneHeadsethook(context: Context) {
        // Simulate a press of the headset button to pick up the call
        val buttonDown = Intent(Intent.ACTION_MEDIA_BUTTON)
        buttonDown.putExtra(Intent.EXTRA_KEY_EVENT, KeyEvent(KeyEvent.ACTION_DOWN, KEYCODE_HEADSETHOOK))
        context.sendOrderedBroadcast(buttonDown, "android.permission.CALL_PRIVILEGED")

        // froyo and beyond trigger on buttonUp instead of buttonDown
        val buttonUp = Intent(Intent.ACTION_MEDIA_BUTTON)
        buttonUp.putExtra(Intent.EXTRA_KEY_EVENT, KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK))
        context.sendOrderedBroadcast(buttonUp, "android.permission.CALL_PRIVILEGED")
    }
}
