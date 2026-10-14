package com.acme.transport.crewing.qualification.qualificationmetric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped qualification summary is in its life cycle. */
enum class CappedQualificationSummaryStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A capped qualification summary as the qualification team keeps it. */
data class CappedQualificationSummary(
    var code: String,
    var name: String,
    var createdAt: Int,
    var updatedAt: Int,
    var routeCode: String,
    var notes: String,
    var reviewer: String,
    var description: String,
    var channel: String,
    var status: CappedQualificationSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedQualificationSummaryStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedQualificationSummaryStatus): Boolean {
        when (status) {
            CappedQualificationSummaryStatus.CREATED -> {
                when (next) {
                    CappedQualificationSummaryStatus.VALIDATED, CappedQualificationSummaryStatus.RETIRED -> return true
                }
                return false
            }
            CappedQualificationSummaryStatus.VALIDATED -> return next == CappedQualificationSummaryStatus.RETIRED
            CappedQualificationSummaryStatus.PUBLISHED -> return next == CappedQualificationSummaryStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: CappedQualificationSummaryStatus) {
        if (!canMoveTo(next)) {
            error("capped qualification summary $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun adjustedTotal(): Double = round((createdAt.toDouble() + updatedAt.toDouble()) * 100) / 100

    fun snapshot(): CappedQualificationSummary {
        return CappedQualificationSummary(
            code = code,
            name = name,
            createdAt = createdAt,
            updatedAt = updatedAt,
            routeCode = routeCode,
            notes = notes,
            reviewer = reviewer,
            description = description,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a capped qualification summary in status created. */
fun newCappedQualificationSummary(code: String, name: String): CappedQualificationSummary {
    return CappedQualificationSummary(
        code = code,
        name = name,
        createdAt = 0,
        updatedAt = 0,
        routeCode = "",
        notes = "",
        reviewer = "",
        description = "",
        channel = "",
        status = CappedQualificationSummaryStatus.CREATED,
    )
}
