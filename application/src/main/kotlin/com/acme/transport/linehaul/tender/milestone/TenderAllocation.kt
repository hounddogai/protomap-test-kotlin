package com.acme.transport.linehaul.tender.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tender allocation is in its life cycle. */
enum class TenderAllocationStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A tender allocation. Its code is unique within a tenant. */
data class TenderAllocation(
    var code: String,
    var label: String,
    var category: String,
    var retries: Int,
    var rate: Double,
    var region: String,
    var channel: String,
    var weight: Double,
    var status: TenderAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TenderAllocationStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TenderAllocationStatus): Boolean {
        when (status) {
            TenderAllocationStatus.CREATED -> return next == TenderAllocationStatus.VALIDATED || next == TenderAllocationStatus.RETIRED
            TenderAllocationStatus.VALIDATED -> {
                when (next) {
                    TenderAllocationStatus.PUBLISHED, TenderAllocationStatus.RETIRED -> return true
                }
                return false
            }
            TenderAllocationStatus.PUBLISHED -> {
                when (next) {
                    TenderAllocationStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TenderAllocationStatus) {
        if (!canMoveTo(next)) {
            error("tender allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun weightedValue(): Double = round((weight + retries.toDouble()) * 100) / 100

    fun snapshot(): TenderAllocation {
        return TenderAllocation(
            code = code,
            label = label,
            category = category,
            retries = retries,
            rate = rate,
            region = region,
            channel = channel,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a tender allocation in status created. */
fun newTenderAllocation(code: String, label: String): TenderAllocation {
    return TenderAllocation(
        code = code,
        label = label,
        category = "",
        retries = 0,
        rate = 0.0,
        region = "",
        channel = "",
        weight = 0.0,
        status = TenderAllocationStatus.CREATED,
    )
}
