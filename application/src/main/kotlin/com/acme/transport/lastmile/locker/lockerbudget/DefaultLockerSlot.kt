package com.acme.transport.lastmile.locker.lockerbudget

import com.acme.transport.kit.text.normalize

/** Where a default locker slot is in its life cycle. */
enum class DefaultLockerSlotStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A default locker slot as the locker team keeps it. */
data class DefaultLockerSlot(
    var code: String,
    var name: String,
    var tags: MutableList<String>,
    var dock: String,
    var segment: String,
    var active: Boolean,
    var quantity: Int,
    var status: DefaultLockerSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultLockerSlotStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultLockerSlotStatus): Boolean {
        when (status) {
            DefaultLockerSlotStatus.NEW -> {
                when (next) {
                    DefaultLockerSlotStatus.QUEUED, DefaultLockerSlotStatus.FAILED -> return true
                }
                return false
            }
            DefaultLockerSlotStatus.QUEUED -> {
                when (next) {
                    DefaultLockerSlotStatus.DONE -> return true
                }
                return false
            }
            DefaultLockerSlotStatus.RUNNING -> {
                when (next) {
                    DefaultLockerSlotStatus.DONE, DefaultLockerSlotStatus.FAILED -> return true
                }
                return false
            }
            DefaultLockerSlotStatus.DONE -> return next == DefaultLockerSlotStatus.FAILED
        }
        return false
    }

    fun moveTo(next: DefaultLockerSlotStatus) {
        if (!canMoveTo(next)) {
            error("default locker slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): DefaultLockerSlot {
        return DefaultLockerSlot(
            code = code,
            name = name,
            tags = tags,
            dock = dock,
            segment = segment,
            active = active,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a default locker slot in status new. */
fun newDefaultLockerSlot(code: String, name: String): DefaultLockerSlot {
    return DefaultLockerSlot(
        code = code,
        name = name,
        tags = mutableListOf<String>(),
        dock = "",
        segment = "",
        active = false,
        quantity = 0,
        status = DefaultLockerSlotStatus.NEW,
    )
}
