package com.acme.transport.fleetops.inspection.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling inspection summary is in its life cycle. */
enum class RollingInspectionSummaryStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A rolling inspection summary. Its code is unique within a tenant. */
data class RollingInspectionSummary(
    var code: String,
    var title: String,
    var approved: Boolean,
    var priority: Int,
    var phone: String,
    var label: String,
    var rate: Double,
    var status: RollingInspectionSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingInspectionSummaryStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingInspectionSummaryStatus): Boolean {
        when (status) {
            RollingInspectionSummaryStatus.CREATED -> return next == RollingInspectionSummaryStatus.PUBLISHED
            RollingInspectionSummaryStatus.VALIDATED -> {
                when (next) {
                    RollingInspectionSummaryStatus.PUBLISHED, RollingInspectionSummaryStatus.RETIRED -> return true
                }
                return false
            }
            RollingInspectionSummaryStatus.PUBLISHED -> {
                when (next) {
                    RollingInspectionSummaryStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingInspectionSummaryStatus) {
        if (!canMoveTo(next)) {
            error("rolling inspection summary $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$title]"

    fun netValue(): Double = round((rate - priority.toDouble()) * 100) / 100

    fun snapshot(): RollingInspectionSummary {
        return RollingInspectionSummary(
            code = code,
            title = title,
            approved = approved,
            priority = priority,
            phone = phone,
            label = label,
            rate = rate,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a rolling inspection summary in status created. */
fun newRollingInspectionSummary(code: String, title: String): RollingInspectionSummary {
    return RollingInspectionSummary(
        code = code,
        title = title,
        approved = false,
        priority = 0,
        phone = "",
        label = "",
        rate = 0.0,
        status = RollingInspectionSummaryStatus.CREATED,
    )
}
