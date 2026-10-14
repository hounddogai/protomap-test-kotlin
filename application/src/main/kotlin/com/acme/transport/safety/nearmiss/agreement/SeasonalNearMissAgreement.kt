package com.acme.transport.safety.nearmiss.agreement

import com.acme.transport.kit.text.normalize

// The seasonal near miss agreement records of safety's near miss work, and the rules, stores, and
// calculations around them.

/** Where a seasonal near miss agreement is in its life cycle. */
enum class SeasonalNearMissAgreementStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a seasonal near miss agreement and its current status. */
data class SeasonalNearMissAgreement(
    var code: String,
    var label: String,
    var archived: Boolean,
    var checksum: String,
    var source: String,
    var title: String,
    var retries: Int,
    var approved: Boolean,
    var status: SeasonalNearMissAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalNearMissAgreementStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalNearMissAgreementStatus): Boolean {
        when (status) {
            SeasonalNearMissAgreementStatus.OPEN -> return next == SeasonalNearMissAgreementStatus.IN_REVIEW || next == SeasonalNearMissAgreementStatus.ARCHIVED
            SeasonalNearMissAgreementStatus.IN_REVIEW -> return next == SeasonalNearMissAgreementStatus.RESOLVED || next == SeasonalNearMissAgreementStatus.ARCHIVED
            SeasonalNearMissAgreementStatus.RESOLVED -> {
                when (next) {
                    SeasonalNearMissAgreementStatus.ARCHIVED -> return true
                }
                return false
            }
            SeasonalNearMissAgreementStatus.REOPENED -> return next == SeasonalNearMissAgreementStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: SeasonalNearMissAgreementStatus) {
        if (!canMoveTo(next)) {
            error("seasonal near miss agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun measuredValue(): Double = retries.toDouble()

    fun snapshot(): SeasonalNearMissAgreement {
        return SeasonalNearMissAgreement(
            code = code,
            label = label,
            archived = archived,
            checksum = checksum,
            source = source,
            title = title,
            retries = retries,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a seasonal near miss agreement in status open. */
fun newSeasonalNearMissAgreement(code: String, label: String): SeasonalNearMissAgreement {
    return SeasonalNearMissAgreement(
        code = code,
        label = label,
        archived = false,
        checksum = "",
        source = "",
        title = "",
        retries = 0,
        approved = false,
        status = SeasonalNearMissAgreementStatus.OPEN,
    )
}
