package com.boom.ems.emsboom

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SwitchCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.realm.Realm


/**
 * Created by nham.manh.tuyen on 14/03/2018.
 */

class ContactAdapter : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    val listItem = mutableListOf<EMSContact>()

    var onCheckChangeListener: OnCheckChangeListener = object : OnCheckChangeListener {
        override fun onCheckChanged(item: EMSContact?, checked: Boolean) {
            Realm.getDefaultInstance().executeTransaction({
                item?.active = checked

                it.copyToRealmOrUpdate(item)
            })
        }
    }

    interface OnCheckChangeListener {
        fun onCheckChanged(item: EMSContact?, checked: Boolean)
    }

    override fun getItemCount(): Int {
        return listItem.size
    }

    fun getItem(position: Int): EMSContact? {
        return listItem.get(position)
    }

    fun addItem(item: EMSContact) {
        listItem.add(item)

        notifyDataSetChanged()
    }

    fun setItem(item: List<EMSContact>) {
        listItem.clear()

        listItem.addAll(item)

        notifyDataSetChanged()
    }

    override fun getItemId(position: Int): Long {
        return listItem.get(position).id
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.contact_item, null)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = getItem(position)

        holder.bindData(contact, onCheckChangeListener)
    }

    class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtPhoneNumber: TextView
        var txtContactName: TextView
        var swActive: SwitchCompat

        var onCheckChangeListener: OnCheckChangeListener? = null
        var contact: EMSContact? = null

        init {
            txtPhoneNumber = itemView.findViewById(R.id.txtPhoneNumber)
            txtContactName = itemView.findViewById(R.id.txtContactName)
            swActive = itemView.findViewById(R.id.swActive)
            swActive.setOnCheckedChangeListener { compoundButton, b ->
                onCheckChangeListener?.onCheckChanged(contact!!, b)
            }
        }

        fun bindData(contact: EMSContact?, onCheckChangeListener: OnCheckChangeListener) {
            this.contact = contact
            this.onCheckChangeListener = onCheckChangeListener

            contact?.apply {
                txtPhoneNumber.text = phoneNumber
                txtContactName.text = name + "-" + id
                swActive.isChecked = active
            }
        }
    }
}
