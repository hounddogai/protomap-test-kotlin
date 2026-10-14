package com.acme.transport.crewing.roster.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched roster mapping is in its life cycle. */
enum class BatchedRosterMappingStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A batched roster mapping as the roster team keeps it. */
data class BatchedRosterMapping(
    var code: String,
    var name: String,
    var revision: Int,
    var minimum: Double,
    var driverId: String,
    var region: String,
    var amount: Double,
    var graceDays: Int,
    var label: String,
    var status: BatchedRosterMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedRosterMappingStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedRosterMappingStatus): Boolean {
        when (status) {
            BatchedRosterMappingStatus.CREATED -> return next == BatchedRosterMappingStatus.VALIDATED || next == BatchedRosterMappingStatus.PUBLISHED
            BatchedRosterMappingStatus.VALIDATED -> {
                when (next) {
                    BatchedRosterMappingStatus.RETIRED -> return true
                }
                return false
            }
            BatchedRosterMappingStatus.PUBLISHED -> return next == BatchedRosterMappingStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: BatchedRosterMappingStatus) {
        if (!canMoveTo(next)) {
            error("batched roster mapping $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code ($name)"

    fun adjustedTotal(): Double = round(revision.toDouble() * minimum * 100) / 100

    fun snapshot(): BatchedRosterMapping {
        return BatchedRosterMapping(
            code = code,
            name = name,
            revision = revision,
            minimum = minimum,
            driverId = driverId,
            region = region,
            amount = amount,
            graceDays = graceDays,
            label = label,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a batched roster mapping in status created. */
fun newBatchedRosterMapping(code: String, name: String): BatchedRosterMapping {
    return BatchedRosterMapping(
        code = code,
        name = name,
        revision = 0,
        minimum = 0.0,
        driverId = "",
        region = "",
        amount = 0.0,
        graceDays = 0,
        label = "",
        status = BatchedRosterMappingStatus.CREATED,
    )
}
