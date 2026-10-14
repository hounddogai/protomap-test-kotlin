package com.acme.transport.network.node.weightednodeagreement

import com.acme.transport.kit.text.normalize

// The global node agreement records of network's node work, and the rules, stores, and calculations around
// them.

/** Where a global node agreement is in its life cycle. */
enum class GlobalNodeAgreementStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One global node agreement, with what the planners need to act on it. */
data class GlobalNodeAgreement(
    var code: String,
    var name: String,
    var segment: String,
    var category: String,
    var phone: String,
    var timezone: String,
    var etaMinutes: Int,
    var contactEmail: String,
    var reviewer: String,
    var status: GlobalNodeAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalNodeAgreementStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalNodeAgreementStatus): Boolean {
        when (status) {
            GlobalNodeAgreementStatus.CREATED -> {
                when (next) {
                    GlobalNodeAgreementStatus.PUBLISHED -> return true
                }
                return false
            }
            GlobalNodeAgreementStatus.VALIDATED -> return next == GlobalNodeAgreementStatus.PUBLISHED || next == GlobalNodeAgreementStatus.RETIRED
            GlobalNodeAgreementStatus.PUBLISHED -> {
                when (next) {
                    GlobalNodeAgreementStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalNodeAgreementStatus) {
        if (!canMoveTo(next)) {
            error("global node agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"global node agreement"} $code"

    fun measuredValue(): Double = etaMinutes.toDouble()

    fun snapshot(): GlobalNodeAgreement {
        return GlobalNodeAgreement(
            code = code,
            name = name,
            segment = segment,
            category = category,
            phone = phone,
            timezone = timezone,
            etaMinutes = etaMinutes,
            contactEmail = contactEmail,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a global node agreement in status created. */
fun newGlobalNodeAgreement(code: String, name: String): GlobalNodeAgreement {
    return GlobalNodeAgreement(
        code = code,
        name = name,
        segment = "",
        category = "",
        phone = "",
        timezone = "",
        etaMinutes = 0,
        contactEmail = "",
        reviewer = "",
        status = GlobalNodeAgreementStatus.CREATED,
    )
}
