package com.acme.transport.terminal.dockdoor.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global dock door template is in its life cycle. */
enum class GlobalDockDoorTemplateStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A global dock door template as the dock door team keeps it. */
data class GlobalDockDoorTemplate(
    var code: String,
    var name: String,
    var stopCount: Int,
    var ownerId: String,
    var approved: Boolean,
    var checksum: String,
    var floorValue: Double,
    var createdAt: Int,
    var status: GlobalDockDoorTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalDockDoorTemplateStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalDockDoorTemplateStatus): Boolean {
        when (status) {
            GlobalDockDoorTemplateStatus.OPEN -> return next == GlobalDockDoorTemplateStatus.IN_REVIEW || next == GlobalDockDoorTemplateStatus.REOPENED
            GlobalDockDoorTemplateStatus.IN_REVIEW -> {
                when (next) {
                    GlobalDockDoorTemplateStatus.REOPENED, GlobalDockDoorTemplateStatus.ARCHIVED -> return true
                }
                return false
            }
            GlobalDockDoorTemplateStatus.RESOLVED -> {
                when (next) {
                    GlobalDockDoorTemplateStatus.REOPENED, GlobalDockDoorTemplateStatus.ARCHIVED -> return true
                }
                return false
            }
            GlobalDockDoorTemplateStatus.REOPENED -> return next == GlobalDockDoorTemplateStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: GlobalDockDoorTemplateStatus) {
        if (!canMoveTo(next)) {
            error("global dock door template $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun adjustedLoad(): Double = round((floorValue + createdAt.toDouble()) * 100) / 100

    fun snapshot(): GlobalDockDoorTemplate {
        return GlobalDockDoorTemplate(
            code = code,
            name = name,
            stopCount = stopCount,
            ownerId = ownerId,
            approved = approved,
            checksum = checksum,
            floorValue = floorValue,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a global dock door template in status open. */
fun newGlobalDockDoorTemplate(code: String, name: String): GlobalDockDoorTemplate {
    return GlobalDockDoorTemplate(
        code = code,
        name = name,
        stopCount = 0,
        ownerId = "",
        approved = false,
        checksum = "",
        floorValue = 0.0,
        createdAt = 0,
        status = GlobalDockDoorTemplateStatus.OPEN,
    )
}
