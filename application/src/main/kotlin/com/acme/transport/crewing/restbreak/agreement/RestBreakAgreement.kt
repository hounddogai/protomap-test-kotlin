package com.acme.transport.crewing.restbreak.agreement

import com.acme.transport.kit.text.normalize

// The rest break agreement records of crewing's rest break work, and the rules, stores, and calculations
// around them.

/** Where a rest break agreement is in its life cycle. */
enum class RestBreakAgreementStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One rest break agreement, with what the planners need to act on it. */
data class RestBreakAgreement(
    var code: String,
    var title: String,
    var checksum: String,
    var etaMinutes: Int,
    var archived: Boolean,
    var region: String,
    var status: RestBreakAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RestBreakAgreementStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RestBreakAgreementStatus): Boolean {
        when (status) {
            RestBreakAgreementStatus.REQUESTED -> return next == RestBreakAgreementStatus.GRANTED || next == RestBreakAgreementStatus.REVOKED
            RestBreakAgreementStatus.GRANTED -> {
                when (next) {
                    RestBreakAgreementStatus.REVOKED, RestBreakAgreementStatus.EXPIRED -> return true
                }
                return false
            }
            RestBreakAgreementStatus.REVOKED -> {
                when (next) {
                    RestBreakAgreementStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RestBreakAgreementStatus) {
        if (!canMoveTo(next)) {
            error("rest break agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun measuredValue(): Double = etaMinutes.toDouble()

    fun snapshot(): RestBreakAgreement {
        return RestBreakAgreement(
            code = code,
            title = title,
            checksum = checksum,
            etaMinutes = etaMinutes,
            archived = archived,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a rest break agreement in status requested. */
fun newRestBreakAgreement(code: String, title: String): RestBreakAgreement {
    return RestBreakAgreement(
        code = code,
        title = title,
        checksum = "",
        etaMinutes = 0,
        archived = false,
        region = "",
        status = RestBreakAgreementStatus.REQUESTED,
    )
}
