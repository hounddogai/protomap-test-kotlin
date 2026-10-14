package com.acme.transport.customs.clearance.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a clearance batch is in its life cycle. */
enum class ClearanceBatchStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a clearance batch and its current status. */
data class ClearanceBatch(
    var code: String,
    var name: String,
    var driverId: String,
    var tags: MutableList<String>,
    var pallets: Int,
    var phone: String,
    var visible: Boolean,
    var segment: String,
    var minimum: Double,
    var revision: Int,
    var channel: String,
    var status: ClearanceBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ClearanceBatchStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ClearanceBatchStatus): Boolean {
        when (status) {
            ClearanceBatchStatus.PROPOSED -> return next == ClearanceBatchStatus.APPROVED
            ClearanceBatchStatus.APPROVED -> {
                when (next) {
                    ClearanceBatchStatus.WITHDRAWN -> return true
                }
                return false
            }
            ClearanceBatchStatus.REJECTED -> {
                when (next) {
                    ClearanceBatchStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ClearanceBatchStatus) {
        if (!canMoveTo(next)) {
            error("clearance batch $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code / $name"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedValue(): Double = round((revision.toDouble() + pallets.toDouble()) * 100) / 100

    fun snapshot(): ClearanceBatch {
        return ClearanceBatch(
            code = code,
            name = name,
            driverId = driverId,
            tags = tags,
            pallets = pallets,
            phone = phone,
            visible = visible,
            segment = segment,
            minimum = minimum,
            revision = revision,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a clearance batch in status proposed. */
fun newClearanceBatch(code: String, name: String): ClearanceBatch {
    return ClearanceBatch(
        code = code,
        name = name,
        driverId = "",
        tags = mutableListOf<String>(),
        pallets = 0,
        phone = "",
        visible = false,
        segment = "",
        minimum = 0.0,
        revision = 0,
        channel = "",
        status = ClearanceBatchStatus.PROPOSED,
    )
}
