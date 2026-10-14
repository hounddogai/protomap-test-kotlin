package com.acme.transport.linehaul.backhaul.request

import com.acme.transport.kit.text.normalize

/** Where a rolling backhaul batch is in its life cycle. */
enum class RollingBackhaulBatchStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A rolling backhaul batch as the backhaul team keeps it. */
data class RollingBackhaulBatch(
    var code: String,
    var name: String,
    var phone: String,
    var source: String,
    var locale: String,
    var reviewer: String,
    var quantity: Int,
    var status: RollingBackhaulBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingBackhaulBatchStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingBackhaulBatchStatus): Boolean {
        when (status) {
            RollingBackhaulBatchStatus.PLANNED -> {
                when (next) {
                    RollingBackhaulBatchStatus.COMPLETED -> return true
                }
                return false
            }
            RollingBackhaulBatchStatus.SCHEDULED -> {
                when (next) {
                    RollingBackhaulBatchStatus.CANCELLED -> return true
                }
                return false
            }
            RollingBackhaulBatchStatus.IN_PROGRESS -> {
                when (next) {
                    RollingBackhaulBatchStatus.CANCELLED -> return true
                }
                return false
            }
            RollingBackhaulBatchStatus.COMPLETED -> return next == RollingBackhaulBatchStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: RollingBackhaulBatchStatus) {
        if (!canMoveTo(next)) {
            error("rolling backhaul batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): RollingBackhaulBatch {
        return RollingBackhaulBatch(
            code = code,
            name = name,
            phone = phone,
            source = source,
            locale = locale,
            reviewer = reviewer,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a rolling backhaul batch in status planned. */
fun newRollingBackhaulBatch(code: String, name: String): RollingBackhaulBatch {
    return RollingBackhaulBatch(
        code = code,
        name = name,
        phone = "",
        source = "",
        locale = "",
        reviewer = "",
        quantity = 0,
        status = RollingBackhaulBatchStatus.PLANNED,
    )
}
