package com.acme.transport.safety.nearmiss.missadjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched near miss mapping is in its life cycle. */
enum class BatchedNearMissMappingStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a batched near miss mapping and its current status. */
data class BatchedNearMissMapping(
    var code: String,
    var name: String,
    var tags: MutableList<String>,
    var reviewer: String,
    var externalRef: String,
    var floorValue: Double,
    var discount: Double,
    var visible: Boolean,
    var status: BatchedNearMissMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedNearMissMappingStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedNearMissMappingStatus): Boolean {
        when (status) {
            BatchedNearMissMappingStatus.CREATED -> return next == BatchedNearMissMappingStatus.VALIDATED || next == BatchedNearMissMappingStatus.PUBLISHED
            BatchedNearMissMappingStatus.VALIDATED -> return next == BatchedNearMissMappingStatus.PUBLISHED || next == BatchedNearMissMappingStatus.RETIRED
            BatchedNearMissMappingStatus.PUBLISHED -> return next == BatchedNearMissMappingStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: BatchedNearMissMappingStatus) {
        if (!canMoveTo(next)) {
            error("batched near miss mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"batched near miss mapping"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun projectedValue(): Double = round((discount - floorValue) * 100) / 100

    fun snapshot(): BatchedNearMissMapping {
        return BatchedNearMissMapping(
            code = code,
            name = name,
            tags = tags,
            reviewer = reviewer,
            externalRef = externalRef,
            floorValue = floorValue,
            discount = discount,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a batched near miss mapping in status created. */
fun newBatchedNearMissMapping(code: String, name: String): BatchedNearMissMapping {
    return BatchedNearMissMapping(
        code = code,
        name = name,
        tags = mutableListOf<String>(),
        reviewer = "",
        externalRef = "",
        floorValue = 0.0,
        discount = 0.0,
        visible = false,
        status = BatchedNearMissMappingStatus.CREATED,
    )
}
