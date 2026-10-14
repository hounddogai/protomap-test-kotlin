package com.acme.transport.crewing.swap.adjustment

import com.acme.transport.kit.text.normalize

/** Where a legacy swap agreement is in its life cycle. */
enum class LegacySwapAgreementStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a legacy swap agreement and its current status. */
data class LegacySwapAgreement(
    var code: String,
    var label: String,
    var stopCount: Int,
    var active: Boolean,
    var checksum: String,
    var title: String,
    var timezone: String,
    var depot: String,
    var status: LegacySwapAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacySwapAgreementStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacySwapAgreementStatus): Boolean {
        when (status) {
            LegacySwapAgreementStatus.IDLE -> {
                when (next) {
                    LegacySwapAgreementStatus.READY, LegacySwapAgreementStatus.DRAINING -> return true
                }
                return false
            }
            LegacySwapAgreementStatus.WARMING -> return next == LegacySwapAgreementStatus.STOPPED
            LegacySwapAgreementStatus.READY -> {
                when (next) {
                    LegacySwapAgreementStatus.DRAINING -> return true
                }
                return false
            }
            LegacySwapAgreementStatus.DRAINING -> return next == LegacySwapAgreementStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: LegacySwapAgreementStatus) {
        if (!canMoveTo(next)) {
            error("legacy swap agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun measuredValue(): Double = stopCount.toDouble()

    fun snapshot(): LegacySwapAgreement {
        return LegacySwapAgreement(
            code = code,
            label = label,
            stopCount = stopCount,
            active = active,
            checksum = checksum,
            title = title,
            timezone = timezone,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a legacy swap agreement in status idle. */
fun newLegacySwapAgreement(code: String, label: String): LegacySwapAgreement {
    return LegacySwapAgreement(
        code = code,
        label = label,
        stopCount = 0,
        active = false,
        checksum = "",
        title = "",
        timezone = "",
        depot = "",
        status = LegacySwapAgreementStatus.IDLE,
    )
}
