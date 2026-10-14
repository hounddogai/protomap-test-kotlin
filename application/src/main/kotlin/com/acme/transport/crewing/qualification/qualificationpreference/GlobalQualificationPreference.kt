package com.acme.transport.crewing.qualification.qualificationpreference

import com.acme.transport.kit.text.normalize

// The global qualification preference records of crewing's qualification work, and the rules, stores, and
// calculations around them.

/** Where a global qualification preference is in its life cycle. */
enum class GlobalQualificationPreferenceStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One global qualification preference, with what the planners need to act on it. */
data class GlobalQualificationPreference(
    var code: String,
    var name: String,
    var createdAt: Int,
    var checksum: String,
    var carrier: String,
    var archived: Boolean,
    var status: GlobalQualificationPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalQualificationPreferenceStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalQualificationPreferenceStatus): Boolean {
        when (status) {
            GlobalQualificationPreferenceStatus.OPEN -> return next == GlobalQualificationPreferenceStatus.IN_REVIEW || next == GlobalQualificationPreferenceStatus.REOPENED
            GlobalQualificationPreferenceStatus.IN_REVIEW -> return next == GlobalQualificationPreferenceStatus.RESOLVED || next == GlobalQualificationPreferenceStatus.REOPENED
            GlobalQualificationPreferenceStatus.RESOLVED -> {
                when (next) {
                    GlobalQualificationPreferenceStatus.REOPENED, GlobalQualificationPreferenceStatus.ARCHIVED -> return true
                }
                return false
            }
            GlobalQualificationPreferenceStatus.REOPENED -> return next == GlobalQualificationPreferenceStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: GlobalQualificationPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("global qualification preference $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"global qualification preference"} $code"

    fun measuredValue(): Double = createdAt.toDouble()

    fun snapshot(): GlobalQualificationPreference {
        return GlobalQualificationPreference(
            code = code,
            name = name,
            createdAt = createdAt,
            checksum = checksum,
            carrier = carrier,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a global qualification preference in status open. */
fun newGlobalQualificationPreference(code: String, name: String): GlobalQualificationPreference {
    return GlobalQualificationPreference(
        code = code,
        name = name,
        createdAt = 0,
        checksum = "",
        carrier = "",
        archived = false,
        status = GlobalQualificationPreferenceStatus.OPEN,
    )
}
