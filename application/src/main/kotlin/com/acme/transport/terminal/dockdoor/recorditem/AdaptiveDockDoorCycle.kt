package com.acme.transport.terminal.dockdoor.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive dock door cycle is in its life cycle. */
enum class AdaptiveDockDoorCycleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A adaptive dock door cycle as the dock door team keeps it. */
data class AdaptiveDockDoorCycle(
    var code: String,
    var label: String,
    var minimum: Double,
    var createdAt: Int,
    var driverId: String,
    var notes: String,
    var contactEmail: String,
    var tags: MutableList<String>,
    var score: Double,
    var ratio: Double,
    var carrier: String,
    var status: AdaptiveDockDoorCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveDockDoorCycleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveDockDoorCycleStatus): Boolean {
        when (status) {
            AdaptiveDockDoorCycleStatus.DRAFT -> {
                when (next) {
                    AdaptiveDockDoorCycleStatus.PENDING, AdaptiveDockDoorCycleStatus.CLOSED -> return true
                }
                return false
            }
            AdaptiveDockDoorCycleStatus.PENDING -> return next == AdaptiveDockDoorCycleStatus.CLOSED
            AdaptiveDockDoorCycleStatus.ACTIVE -> {
                when (next) {
                    AdaptiveDockDoorCycleStatus.CLOSED -> return true
                }
                return false
            }
            AdaptiveDockDoorCycleStatus.SUSPENDED -> {
                when (next) {
                    AdaptiveDockDoorCycleStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveDockDoorCycleStatus) {
        if (!canMoveTo(next)) {
            error("adaptive dock door cycle $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedLoad(): Double = round(ratio * minimum * 100) / 100

    fun snapshot(): AdaptiveDockDoorCycle {
        return AdaptiveDockDoorCycle(
            code = code,
            label = label,
            minimum = minimum,
            createdAt = createdAt,
            driverId = driverId,
            notes = notes,
            contactEmail = contactEmail,
            tags = tags,
            score = score,
            ratio = ratio,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a adaptive dock door cycle in status draft. */
fun newAdaptiveDockDoorCycle(code: String, label: String): AdaptiveDockDoorCycle {
    return AdaptiveDockDoorCycle(
        code = code,
        label = label,
        minimum = 0.0,
        createdAt = 0,
        driverId = "",
        notes = "",
        contactEmail = "",
        tags = mutableListOf<String>(),
        score = 0.0,
        ratio = 0.0,
        carrier = "",
        status = AdaptiveDockDoorCycleStatus.DRAFT,
    )
}
