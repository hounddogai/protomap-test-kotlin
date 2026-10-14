package com.acme.transport.crewing.swap.recorditem

import com.acme.transport.kit.text.normalize

/** Where a manual swap score is in its life cycle. */
enum class ManualSwapScoreStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A manual swap score as the swap team keeps it. */
data class ManualSwapScore(
    var code: String,
    var title: String,
    var checksum: String,
    var discount: Double,
    var vehicleId: String,
    var status: ManualSwapScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualSwapScoreStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualSwapScoreStatus): Boolean {
        when (status) {
            ManualSwapScoreStatus.DRAFT -> {
                when (next) {
                    ManualSwapScoreStatus.PENDING -> return true
                }
                return false
            }
            ManualSwapScoreStatus.PENDING -> return next == ManualSwapScoreStatus.SUSPENDED || next == ManualSwapScoreStatus.CLOSED
            ManualSwapScoreStatus.ACTIVE -> return next == ManualSwapScoreStatus.SUSPENDED
            ManualSwapScoreStatus.SUSPENDED -> return next == ManualSwapScoreStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: ManualSwapScoreStatus) {
        if (!canMoveTo(next)) {
            error("manual swap score $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun measuredValue(): Double = discount

    fun snapshot(): ManualSwapScore {
        return ManualSwapScore(
            code = code,
            title = title,
            checksum = checksum,
            discount = discount,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a manual swap score in status draft. */
fun newManualSwapScore(code: String, title: String): ManualSwapScore {
    return ManualSwapScore(
        code = code,
        title = title,
        checksum = "",
        discount = 0.0,
        vehicleId = "",
        status = ManualSwapScoreStatus.DRAFT,
    )
}
