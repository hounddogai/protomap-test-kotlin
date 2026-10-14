package com.acme.transport.network.servicearea.areabatch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The regional service area batch records of network's service area work, and the rules, stores, and
// calculations around them.

/** Where a regional service area batch is in its life cycle. */
enum class RegionalServiceAreaBatchStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a regional service area batch and its current status. */
data class RegionalServiceAreaBatch(
    var code: String,
    var name: String,
    var batchSize: Int,
    var revision: Int,
    var phone: String,
    var floorValue: Double,
    var capacity: Int,
    var status: RegionalServiceAreaBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalServiceAreaBatchStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalServiceAreaBatchStatus): Boolean {
        when (status) {
            RegionalServiceAreaBatchStatus.OPEN -> {
                when (next) {
                    RegionalServiceAreaBatchStatus.IN_REVIEW -> return true
                }
                return false
            }
            RegionalServiceAreaBatchStatus.IN_REVIEW -> return next == RegionalServiceAreaBatchStatus.REOPENED || next == RegionalServiceAreaBatchStatus.ARCHIVED
            RegionalServiceAreaBatchStatus.RESOLVED -> return next == RegionalServiceAreaBatchStatus.REOPENED
            RegionalServiceAreaBatchStatus.REOPENED -> {
                when (next) {
                    RegionalServiceAreaBatchStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalServiceAreaBatchStatus) {
        if (!canMoveTo(next)) {
            error("regional service area batch $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code [$name]"

    fun adjustedVolume(): Double = round(batchSize.toDouble() * floorValue * 100) / 100

    fun snapshot(): RegionalServiceAreaBatch {
        return RegionalServiceAreaBatch(
            code = code,
            name = name,
            batchSize = batchSize,
            revision = revision,
            phone = phone,
            floorValue = floorValue,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a regional service area batch in status open. */
fun newRegionalServiceAreaBatch(code: String, name: String): RegionalServiceAreaBatch {
    return RegionalServiceAreaBatch(
        code = code,
        name = name,
        batchSize = 0,
        revision = 0,
        phone = "",
        floorValue = 0.0,
        capacity = 0,
        status = RegionalServiceAreaBatchStatus.OPEN,
    )
}
