package com.acme.transport.fleetops.serviceplan.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient service plan summary is in its life cycle. */
enum class LenientServicePlanSummaryStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a lenient service plan summary and its current status. */
data class LenientServicePlanSummary(
    var code: String,
    var name: String,
    var checksum: String,
    var externalRef: String,
    var retries: Int,
    var updatedAt: Int,
    var weight: Double,
    var channel: String,
    var locale: String,
    var description: String,
    var status: LenientServicePlanSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientServicePlanSummaryStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientServicePlanSummaryStatus): Boolean {
        when (status) {
            LenientServicePlanSummaryStatus.PLANNED -> {
                when (next) {
                    LenientServicePlanSummaryStatus.COMPLETED, LenientServicePlanSummaryStatus.CANCELLED -> return true
                }
                return false
            }
            LenientServicePlanSummaryStatus.SCHEDULED -> {
                when (next) {
                    LenientServicePlanSummaryStatus.IN_PROGRESS -> return true
                }
                return false
            }
            LenientServicePlanSummaryStatus.IN_PROGRESS -> {
                when (next) {
                    LenientServicePlanSummaryStatus.CANCELLED -> return true
                }
                return false
            }
            LenientServicePlanSummaryStatus.COMPLETED -> {
                when (next) {
                    LenientServicePlanSummaryStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientServicePlanSummaryStatus) {
        if (!canMoveTo(next)) {
            error("lenient service plan summary $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"lenient service plan summary"} $code"

    fun netValue(): Double = round((retries.toDouble() + updatedAt.toDouble()) * 100) / 100

    fun snapshot(): LenientServicePlanSummary {
        return LenientServicePlanSummary(
            code = code,
            name = name,
            checksum = checksum,
            externalRef = externalRef,
            retries = retries,
            updatedAt = updatedAt,
            weight = weight,
            channel = channel,
            locale = locale,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a lenient service plan summary in status planned. */
fun newLenientServicePlanSummary(code: String, name: String): LenientServicePlanSummary {
    return LenientServicePlanSummary(
        code = code,
        name = name,
        checksum = "",
        externalRef = "",
        retries = 0,
        updatedAt = 0,
        weight = 0.0,
        channel = "",
        locale = "",
        description = "",
        status = LenientServicePlanSummaryStatus.PLANNED,
    )
}
