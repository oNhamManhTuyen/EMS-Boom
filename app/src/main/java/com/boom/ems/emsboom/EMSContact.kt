package com.boom.ems.emsboom

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required


/**
 * Created by nham.manh.tuyen on 14/03/2018.
 */
open class EMSContact(@PrimaryKey
                      var id: Long = 0,

                      @Required
                      var phoneNumber: String? = null,

                      var name: String? = null,

                      var active: Boolean = true) : RealmObject()