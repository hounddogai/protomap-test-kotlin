package com.acme.transport.crewing.roster.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a roster agreement is in its life cycle. */
enum class RosterAgreementStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A roster agreement. Its code is unique within a tenant. */
data class RosterAgreement(
    var code: String,
    var name: String,
    var updatedAt: Int,
    var locale: String,
    var priority: Int,
    var minimum: Double,
    var category: String,
    var title: String,
    var status: RosterAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RosterAgreementStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RosterAgreementStatus): Boolean {
        when (status) {
            RosterAgreementStatus.REQUESTED -> return next == RosterAgreementStatus.EXPIRED
            RosterAgreementStatus.GRANTED -> {
                when (next) {
                    RosterAgreementStatus.REVOKED, RosterAgreementStatus.EXPIRED -> return true
                }
                return false
            }
            RosterAgreementStatus.REVOKED -> {
                when (next) {
                    RosterAgreementStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RosterAgreementStatus) {
        if (!canMoveTo(next)) {
            error("roster agreement $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun netExposure(): Double = round(updatedAt.toDouble() * priority.toDouble() * 100) / 100

    fun snapshot(): RosterAgreement {
        return RosterAgreement(
            code = code,
            name = name,
            updatedAt = updatedAt,
            locale = locale,
            priority = priority,
            minimum = minimum,
            category = category,
            title = title,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a roster agreement in status requested. */
fun newRosterAgreement(code: String, name: String): RosterAgreement {
    return RosterAgreement(
        code = code,
        name = name,
        updatedAt = 0,
        locale = "",
        priority = 0,
        minimum = 0.0,
        category = "",
        title = "",
        status = RosterAgreementStatus.REQUESTED,
    )
}
