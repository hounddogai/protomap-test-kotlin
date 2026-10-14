package com.acme.transport.customs.hold.checkpoint

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped hold override is in its life cycle. */
enum class CappedHoldOverrideStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A capped hold override. Its code is unique within a tenant. */
data class CappedHoldOverride(
    var code: String,
    var label: String,
    var notes: String,
    var stopCount: Int,
    var quantity: Int,
    var segment: String,
    var revision: Int,
    var capacity: Int,
    var status: CappedHoldOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedHoldOverrideStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedHoldOverrideStatus): Boolean {
        when (status) {
            CappedHoldOverrideStatus.NEW -> {
                when (next) {
                    CappedHoldOverrideStatus.DONE, CappedHoldOverrideStatus.FAILED -> return true
                }
                return false
            }
            CappedHoldOverrideStatus.QUEUED -> return next == CappedHoldOverrideStatus.DONE
            CappedHoldOverrideStatus.RUNNING -> {
                when (next) {
                    CappedHoldOverrideStatus.DONE, CappedHoldOverrideStatus.FAILED -> return true
                }
                return false
            }
            CappedHoldOverrideStatus.DONE -> return next == CappedHoldOverrideStatus.FAILED
        }
        return false
    }

    fun moveTo(next: CappedHoldOverrideStatus) {
        if (!canMoveTo(next)) {
            error("capped hold override $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code [$label]"

    fun adjustedLoad(): Double = round((capacity.toDouble() + quantity.toDouble()) * 100) / 100

    fun snapshot(): CappedHoldOverride {
        return CappedHoldOverride(
            code = code,
            label = label,
            notes = notes,
            stopCount = stopCount,
            quantity = quantity,
            segment = segment,
            revision = revision,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a capped hold override in status new. */
fun newCappedHoldOverride(code: String, label: String): CappedHoldOverride {
    return CappedHoldOverride(
        code = code,
        label = label,
        notes = "",
        stopCount = 0,
        quantity = 0,
        segment = "",
        revision = 0,
        capacity = 0,
        status = CappedHoldOverrideStatus.NEW,
    )
}
