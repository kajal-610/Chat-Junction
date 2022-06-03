package com.example.cj.Models

import java.util.ArrayList

class UserStatus {
    var name: String? = null
    var profileImage: String? = null
    var lastUpdated: Long = 0
    var statuses: ArrayList<Status>? = null

    constructor() {}
    constructor(
        name: String?,
        profileImage: String?,
        lastUpdated: Long,
        statuses: ArrayList<Status>?
    ) {
        this.name = name
        this.profileImage = profileImage
        this.lastUpdated = lastUpdated
        this.statuses = statuses
    }
}