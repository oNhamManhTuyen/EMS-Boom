package com.boom.ems.emsboom

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Patterns
import android.view.Menu
import android.view.MenuItem
import android.view.View
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), View.OnClickListener {

    private val PERMISSIONS_REQUEST_READ_PHONE_STATE: Int = 1000
    private val PERMISSIONS_REQUEST_READ_CONTACTS: Int = 1001

    private lateinit var realm: Realm
    private lateinit var contactAdapter: ContactAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        contactAdapter = ContactAdapter()
        rcvContactList.adapter = contactAdapter

        setupRealm()

        checkPermission()

        updateUI(readContact())
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.READ_PHONE_STATE),
                    PERMISSIONS_REQUEST_READ_PHONE_STATE)
            return
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.READ_CONTACTS),
                    PERMISSIONS_REQUEST_READ_CONTACTS)
        } else {
            updateUI(readContact())
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSIONS_REQUEST_READ_PHONE_STATE -> {
                if (grantResults.size > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkPermission()
                }
            }
        }
    }

    private fun updateUI(contactList: List<EMSContact>?) {
        contactList?.let {
            contactAdapter.setItem(it)
            setTitle(getString(R.string.app_name) + "(" + contactList.size + ")")
        }
    }

    override fun onClick(v: View) {
        showDialogInputNumber()
    }

    private fun addContact(id: Long, phoneNumber: String, contactName: String = phoneNumber) {
        if (Patterns.PHONE.matcher(phoneNumber).matches()) {

            realm.executeTransaction({
                val contactFound = realm.where(EMSContact::class.java).equalTo("id", id).count()
                if (contactFound <= 0) {
                    val newContact = EMSContact(
                            id = id,
                            phoneNumber = phoneNumber,
                            name = contactName)

                    realm.copyToRealmOrUpdate(newContact)
                }
            })
        }
    }

    private fun readContact(): List<EMSContact>? {
        try {
            realm.beginTransaction()
            val allContact = realm.where(EMSContact::class.java).findAll()
            realm.commitTransaction()

            return allContact.toList()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    private fun setupRealm() {
        realm = Realm.getDefaultInstance()
    }

    private fun showDialogInputNumber() {
//        val builder = AlertDialog.Builder(this)
//        builder.setTitle(getString(R.string.input_number))
//
//        val input = EditText(this)
//        input.inputType = InputType.TYPE_CLASS_PHONE
//        builder.setView(input)
//
//        builder.setPositiveButton("OK", { dialog, which ->
//            addContact(phoneNumber = String, input.text.toString())
//
//            updateUI(readContact())
//        })
//
//        builder.setNegativeButton("Cancel", { dialog, which -> dialog.cancel() })
//
//        builder.show()
    }

    private fun syncPhoneContact() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val cr = getContentResolver()
        val cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null)
        cur?.apply {
            if (getCount() > 0) {

                while (cur.moveToNext()) {
                    val id = cur.getString(
                            cur.getColumnIndex(ContactsContract.Contacts._ID))

                    if (cur.getInt(cur.getColumnIndex(
                                    ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                        val pCur = cr.query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                arrayOf(id), ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC")
                        while (pCur.moveToNext()) {
                            val phoneNo = pCur.getString(pCur.getColumnIndex(
                                    ContactsContract.CommonDataKinds.Phone.NUMBER))
                            val contactName = pCur.getString(pCur.getColumnIndex(
                                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                            addContact(id = id.toLong(), phoneNumber = phoneNo, contactName = contactName)
                        }

                        pCur.close()
                    }
                }
            }
            cur.close();
        }
    }

    private fun deleteAllRecord() {
        Realm.getDefaultInstance().executeTransaction {
            it.deleteAll()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.getItemId() == R.id.action_sync_contact) {
//            deleteAllRecord()

            syncPhoneContact()

            updateUI(readContact())
        }
        return true
    }
}
