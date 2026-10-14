package com.acme.transport.customs.declaration.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a declaration schedule is in its life cycle. */
enum class DeclarationScheduleStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A declaration schedule as the declaration team keeps it. */
data class DeclarationSchedule(
    var code: String,
    var name: String,
    var contactEmail: String,
    var tags: MutableList<String>,
    var fuelLiters: Double,
    var segment: String,
    var minimum: Double,
    var status: DeclarationScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DeclarationScheduleStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DeclarationScheduleStatus): Boolean {
        when (status) {
            DeclarationScheduleStatus.IDLE -> {
                when (next) {
                    DeclarationScheduleStatus.WARMING -> return true
                }
                return false
            }
            DeclarationScheduleStatus.WARMING -> return next == DeclarationScheduleStatus.DRAINING || next == DeclarationScheduleStatus.STOPPED
            DeclarationScheduleStatus.READY -> {
                when (next) {
                    DeclarationScheduleStatus.DRAINING, DeclarationScheduleStatus.STOPPED -> return true
                }
                return false
            }
            DeclarationScheduleStatus.DRAINING -> {
                when (next) {
                    DeclarationScheduleStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DeclarationScheduleStatus) {
        if (!canMoveTo(next)) {
            error("declaration schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun projectedValue(): Double = round(minimum * fuelLiters * 100) / 100

    fun snapshot(): DeclarationSchedule {
        return DeclarationSchedule(
            code = code,
            name = name,
            contactEmail = contactEmail,
            tags = tags,
            fuelLiters = fuelLiters,
            segment = segment,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a declaration schedule in status idle. */
fun newDeclarationSchedule(code: String, name: String): DeclarationSchedule {
    return DeclarationSchedule(
        code = code,
        name = name,
        contactEmail = "",
        tags = mutableListOf<String>(),
        fuelLiters = 0.0,
        segment = "",
        minimum = 0.0,
        status = DeclarationScheduleStatus.IDLE,
    )
}
