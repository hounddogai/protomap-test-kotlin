package com.acme.transport.terminal.dockdoor.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The dock door threshold records of terminal's dock door work, and the rules, stores, and calculations
// around them.

/** Where a dock door threshold is in its life cycle. */
enum class DockDoorThresholdStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A dock door threshold as the dock door team keeps it. */
data class DockDoorThreshold(
    var code: String,
    var name: String,
    var active: Boolean,
    var routeCode: String,
    var phone: String,
    var ratio: Double,
    var rate: Double,
    var endsAt: Int,
    var channel: String,
    var status: DockDoorThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DockDoorThresholdStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: DockDoorThresholdStatus): Boolean {
        when (status) {
            DockDoorThresholdStatus.DRAFT -> {
                when (next) {
                    DockDoorThresholdStatus.ACTIVE -> return true
                }
                return false
            }
            DockDoorThresholdStatus.PENDING -> {
                when (next) {
                    DockDoorThresholdStatus.ACTIVE -> return true
                }
                return false
            }
            DockDoorThresholdStatus.ACTIVE -> {
                when (next) {
                    DockDoorThresholdStatus.SUSPENDED, DockDoorThresholdStatus.CLOSED -> return true
                }
                return false
            }
            DockDoorThresholdStatus.SUSPENDED -> return next == DockDoorThresholdStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: DockDoorThresholdStatus) {
        if (!canMoveTo(next)) {
            error("dock door threshold $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun effectiveLoad(): Double = round((rate - ratio) * 100) / 100

    fun snapshot(): DockDoorThreshold {
        return DockDoorThreshold(
            code = code,
            name = name,
            active = active,
            routeCode = routeCode,
            phone = phone,
            ratio = ratio,
            rate = rate,
            endsAt = endsAt,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a dock door threshold in status draft. */
fun newDockDoorThreshold(code: String, name: String): DockDoorThreshold {
    return DockDoorThreshold(
        code = code,
        name = name,
        active = false,
        routeCode = "",
        phone = "",
        ratio = 0.0,
        rate = 0.0,
        endsAt = 0,
        channel = "",
        status = DockDoorThresholdStatus.DRAFT,
    )
}
