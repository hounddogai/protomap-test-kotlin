package com.acme.transport.customs.clearance.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a clearance window is in its life cycle. */
enum class ClearanceWindowStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A clearance window. Its code is unique within a tenant. */
data class ClearanceWindow(
    var code: String,
    var label: String,
    var score: Double,
    var carrier: String,
    var maximum: Double,
    var dock: String,
    var threshold: Double,
    var checksum: String,
    var status: ClearanceWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ClearanceWindowStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ClearanceWindowStatus): Boolean {
        when (status) {
            ClearanceWindowStatus.DRAFT -> {
                when (next) {
                    ClearanceWindowStatus.SUSPENDED -> return true
                }
                return false
            }
            ClearanceWindowStatus.PENDING -> {
                when (next) {
                    ClearanceWindowStatus.SUSPENDED, ClearanceWindowStatus.CLOSED -> return true
                }
                return false
            }
            ClearanceWindowStatus.ACTIVE -> {
                when (next) {
                    ClearanceWindowStatus.SUSPENDED, ClearanceWindowStatus.CLOSED -> return true
                }
                return false
            }
            ClearanceWindowStatus.SUSPENDED -> {
                when (next) {
                    ClearanceWindowStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ClearanceWindowStatus) {
        if (!canMoveTo(next)) {
            error("clearance window $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun netTotal(): Double = round(maximum * score * 100) / 100

    fun snapshot(): ClearanceWindow {
        return ClearanceWindow(
            code = code,
            label = label,
            score = score,
            carrier = carrier,
            maximum = maximum,
            dock = dock,
            threshold = threshold,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a clearance window in status draft. */
fun newClearanceWindow(code: String, label: String): ClearanceWindow {
    return ClearanceWindow(
        code = code,
        label = label,
        score = 0.0,
        carrier = "",
        maximum = 0.0,
        dock = "",
        threshold = 0.0,
        checksum = "",
        status = ClearanceWindowStatus.DRAFT,
    )
}
