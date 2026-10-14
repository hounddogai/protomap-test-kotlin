package com.acme.transport.fleetops.utilization.utilizationsegment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback utilization summary is in its life cycle. */
enum class FallbackUtilizationSummaryStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A fallback utilization summary. Its code is unique within a tenant. */
data class FallbackUtilizationSummary(
    var code: String,
    var label: String,
    var checksum: String,
    var threshold: Double,
    var attempts: Int,
    var carrier: String,
    var score: Double,
    var attributes: MutableMap<String, String>,
    var status: FallbackUtilizationSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackUtilizationSummaryStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackUtilizationSummaryStatus): Boolean {
        when (status) {
            FallbackUtilizationSummaryStatus.DRAFT -> {
                when (next) {
                    FallbackUtilizationSummaryStatus.SUSPENDED -> return true
                }
                return false
            }
            FallbackUtilizationSummaryStatus.PENDING -> return next == FallbackUtilizationSummaryStatus.ACTIVE || next == FallbackUtilizationSummaryStatus.SUSPENDED
            FallbackUtilizationSummaryStatus.ACTIVE -> {
                when (next) {
                    FallbackUtilizationSummaryStatus.SUSPENDED -> return true
                }
                return false
            }
            FallbackUtilizationSummaryStatus.SUSPENDED -> {
                when (next) {
                    FallbackUtilizationSummaryStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackUtilizationSummaryStatus) {
        if (!canMoveTo(next)) {
            error("fallback utilization summary $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedLoad(): Double = round(score * attempts.toDouble() * 100) / 100

    fun snapshot(): FallbackUtilizationSummary {
        return FallbackUtilizationSummary(
            code = code,
            label = label,
            checksum = checksum,
            threshold = threshold,
            attempts = attempts,
            carrier = carrier,
            score = score,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a fallback utilization summary in status draft. */
fun newFallbackUtilizationSummary(code: String, label: String): FallbackUtilizationSummary {
    return FallbackUtilizationSummary(
        code = code,
        label = label,
        checksum = "",
        threshold = 0.0,
        attempts = 0,
        carrier = "",
        score = 0.0,
        attributes = mutableMapOf<String, String>(),
        status = FallbackUtilizationSummaryStatus.DRAFT,
    )
}
