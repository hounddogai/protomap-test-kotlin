package com.acme.transport.terminal.dockdoor.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped dock door threshold is in its life cycle. */
enum class CappedDockDoorThresholdStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A capped dock door threshold. Its code is unique within a tenant. */
data class CappedDockDoorThreshold(
    var code: String,
    var name: String,
    var durationMinutes: Int,
    var label: String,
    var stopCount: Int,
    var endsAt: Int,
    var title: String,
    var depot: String,
    var expiresAt: Int,
    var status: CappedDockDoorThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedDockDoorThresholdStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedDockDoorThresholdStatus): Boolean {
        when (status) {
            CappedDockDoorThresholdStatus.CREATED -> {
                when (next) {
                    CappedDockDoorThresholdStatus.PUBLISHED, CappedDockDoorThresholdStatus.RETIRED -> return true
                }
                return false
            }
            CappedDockDoorThresholdStatus.VALIDATED -> {
                when (next) {
                    CappedDockDoorThresholdStatus.PUBLISHED, CappedDockDoorThresholdStatus.RETIRED -> return true
                }
                return false
            }
            CappedDockDoorThresholdStatus.PUBLISHED -> return next == CappedDockDoorThresholdStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: CappedDockDoorThresholdStatus) {
        if (!canMoveTo(next)) {
            error("capped dock door threshold $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($name)"

    fun adjustedValue(): Double = round((endsAt.toDouble() - stopCount.toDouble()) * 100) / 100

    fun snapshot(): CappedDockDoorThreshold {
        return CappedDockDoorThreshold(
            code = code,
            name = name,
            durationMinutes = durationMinutes,
            label = label,
            stopCount = stopCount,
            endsAt = endsAt,
            title = title,
            depot = depot,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a capped dock door threshold in status created. */
fun newCappedDockDoorThreshold(code: String, name: String): CappedDockDoorThreshold {
    return CappedDockDoorThreshold(
        code = code,
        name = name,
        durationMinutes = 0,
        label = "",
        stopCount = 0,
        endsAt = 0,
        title = "",
        depot = "",
        expiresAt = 0,
        status = CappedDockDoorThresholdStatus.CREATED,
    )
}
