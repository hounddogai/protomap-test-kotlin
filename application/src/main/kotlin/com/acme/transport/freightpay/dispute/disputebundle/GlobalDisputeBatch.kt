package com.acme.transport.freightpay.dispute.disputebundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global dispute batch is in its life cycle. */
enum class GlobalDisputeBatchStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One global dispute batch, with what the planners need to act on it. */
data class GlobalDisputeBatch(
    var code: String,
    var title: String,
    var checksum: String,
    var odometer: Int,
    var budget: Double,
    var currency: String,
    var endsAt: Int,
    var dock: String,
    var shiftCode: String,
    var status: GlobalDisputeBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalDisputeBatchStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalDisputeBatchStatus): Boolean {
        when (status) {
            GlobalDisputeBatchStatus.CREATED -> {
                when (next) {
                    GlobalDisputeBatchStatus.PUBLISHED -> return true
                }
                return false
            }
            GlobalDisputeBatchStatus.VALIDATED -> {
                when (next) {
                    GlobalDisputeBatchStatus.PUBLISHED, GlobalDisputeBatchStatus.RETIRED -> return true
                }
                return false
            }
            GlobalDisputeBatchStatus.PUBLISHED -> {
                when (next) {
                    GlobalDisputeBatchStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalDisputeBatchStatus) {
        if (!canMoveTo(next)) {
            error("global dispute batch $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"global dispute batch"} $code"

    fun netValue(): Double = round(budget * odometer.toDouble() * 100) / 100

    fun snapshot(): GlobalDisputeBatch {
        return GlobalDisputeBatch(
            code = code,
            title = title,
            checksum = checksum,
            odometer = odometer,
            budget = budget,
            currency = currency,
            endsAt = endsAt,
            dock = dock,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a global dispute batch in status created. */
fun newGlobalDisputeBatch(code: String, title: String): GlobalDisputeBatch {
    return GlobalDisputeBatch(
        code = code,
        title = title,
        checksum = "",
        odometer = 0,
        budget = 0.0,
        currency = "",
        endsAt = 0,
        dock = "",
        shiftCode = "",
        status = GlobalDisputeBatchStatus.CREATED,
    )
}
