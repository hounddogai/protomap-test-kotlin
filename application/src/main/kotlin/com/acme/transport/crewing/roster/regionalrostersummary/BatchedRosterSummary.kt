package com.acme.transport.crewing.roster.regionalrostersummary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The batched roster summary records of crewing's roster work, and the rules, stores, and calculations around
// them.

/** Where a batched roster summary is in its life cycle. */
enum class BatchedRosterSummaryStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a batched roster summary and its current status. */
data class BatchedRosterSummary(
    var code: String,
    var label: String,
    var odometer: Int,
    var approved: Boolean,
    var attributes: MutableMap<String, String>,
    var revision: Int,
    var etaMinutes: Int,
    var loadKg: Double,
    var currency: String,
    var margin: Double,
    var status: BatchedRosterSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedRosterSummaryStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedRosterSummaryStatus): Boolean {
        when (status) {
            BatchedRosterSummaryStatus.PROPOSED -> {
                when (next) {
                    BatchedRosterSummaryStatus.APPROVED, BatchedRosterSummaryStatus.WITHDRAWN -> return true
                }
                return false
            }
            BatchedRosterSummaryStatus.APPROVED -> return next == BatchedRosterSummaryStatus.REJECTED || next == BatchedRosterSummaryStatus.WITHDRAWN
            BatchedRosterSummaryStatus.REJECTED -> return next == BatchedRosterSummaryStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: BatchedRosterSummaryStatus) {
        if (!canMoveTo(next)) {
            error("batched roster summary $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code ($label)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveLoad(): Double = round((revision.toDouble() + odometer.toDouble()) * 100) / 100

    fun snapshot(): BatchedRosterSummary {
        return BatchedRosterSummary(
            code = code,
            label = label,
            odometer = odometer,
            approved = approved,
            attributes = attributes,
            revision = revision,
            etaMinutes = etaMinutes,
            loadKg = loadKg,
            currency = currency,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a batched roster summary in status proposed. */
fun newBatchedRosterSummary(code: String, label: String): BatchedRosterSummary {
    return BatchedRosterSummary(
        code = code,
        label = label,
        odometer = 0,
        approved = false,
        attributes = mutableMapOf<String, String>(),
        revision = 0,
        etaMinutes = 0,
        loadKg = 0.0,
        currency = "",
        margin = 0.0,
        status = BatchedRosterSummaryStatus.PROPOSED,
    )
}
