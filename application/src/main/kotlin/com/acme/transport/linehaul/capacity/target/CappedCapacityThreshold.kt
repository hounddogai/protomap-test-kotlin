package com.acme.transport.linehaul.capacity.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped capacity threshold is in its life cycle. */
enum class CappedCapacityThresholdStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A capped capacity threshold. Its code is unique within a tenant. */
data class CappedCapacityThreshold(
    var code: String,
    var label: String,
    var contactEmail: String,
    var endsAt: Int,
    var phone: String,
    var timezone: String,
    var attempts: Int,
    var batchSize: Int,
    var status: CappedCapacityThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedCapacityThresholdStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedCapacityThresholdStatus): Boolean {
        when (status) {
            CappedCapacityThresholdStatus.IDLE -> {
                when (next) {
                    CappedCapacityThresholdStatus.DRAINING, CappedCapacityThresholdStatus.STOPPED -> return true
                }
                return false
            }
            CappedCapacityThresholdStatus.WARMING -> return next == CappedCapacityThresholdStatus.READY || next == CappedCapacityThresholdStatus.STOPPED
            CappedCapacityThresholdStatus.READY -> {
                when (next) {
                    CappedCapacityThresholdStatus.DRAINING, CappedCapacityThresholdStatus.STOPPED -> return true
                }
                return false
            }
            CappedCapacityThresholdStatus.DRAINING -> return next == CappedCapacityThresholdStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: CappedCapacityThresholdStatus) {
        if (!canMoveTo(next)) {
            error("capped capacity threshold $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun effectiveVolume(): Double = round(batchSize.toDouble() * endsAt.toDouble() * 100) / 100

    fun snapshot(): CappedCapacityThreshold {
        return CappedCapacityThreshold(
            code = code,
            label = label,
            contactEmail = contactEmail,
            endsAt = endsAt,
            phone = phone,
            timezone = timezone,
            attempts = attempts,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a capped capacity threshold in status idle. */
fun newCappedCapacityThreshold(code: String, label: String): CappedCapacityThreshold {
    return CappedCapacityThreshold(
        code = code,
        label = label,
        contactEmail = "",
        endsAt = 0,
        phone = "",
        timezone = "",
        attempts = 0,
        batchSize = 0,
        status = CappedCapacityThresholdStatus.IDLE,
    )
}
