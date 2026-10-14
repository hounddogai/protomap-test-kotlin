package com.acme.transport.linehaul.capacity.summary

import com.acme.transport.kit.text.normalize

// The capacity summary records of linehaul's capacity work, and the rules, stores, and calculations around
// them.

/** Where a capacity summary is in its life cycle. */
enum class CapacitySummaryStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A capacity summary. Its code is unique within a tenant. */
data class CapacitySummary(
    var code: String,
    var name: String,
    var segment: String,
    var updatedAt: Int,
    var reviewer: String,
    var status: CapacitySummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CapacitySummaryStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CapacitySummaryStatus): Boolean {
        when (status) {
            CapacitySummaryStatus.CREATED -> {
                when (next) {
                    CapacitySummaryStatus.VALIDATED -> return true
                }
                return false
            }
            CapacitySummaryStatus.VALIDATED -> return next == CapacitySummaryStatus.RETIRED
            CapacitySummaryStatus.PUBLISHED -> {
                when (next) {
                    CapacitySummaryStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CapacitySummaryStatus) {
        if (!canMoveTo(next)) {
            error("capacity summary $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun measuredValue(): Double = updatedAt.toDouble()

    fun snapshot(): CapacitySummary {
        return CapacitySummary(
            code = code,
            name = name,
            segment = segment,
            updatedAt = updatedAt,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a capacity summary in status created. */
fun newCapacitySummary(code: String, name: String): CapacitySummary {
    return CapacitySummary(
        code = code,
        name = name,
        segment = "",
        updatedAt = 0,
        reviewer = "",
        status = CapacitySummaryStatus.CREATED,
    )
}
