package com.acme.transport.terminal.dockdoor.tiereddooragreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling dock door template is in its life cycle. */
enum class RollingDockDoorTemplateStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a rolling dock door template and its current status. */
data class RollingDockDoorTemplate(
    var code: String,
    var title: String,
    var ratio: Double,
    var active: Boolean,
    var trailerId: String,
    var endsAt: Int,
    var durationMinutes: Int,
    var floorValue: Double,
    var country: String,
    var contactEmail: String,
    var pinned: Boolean,
    var status: RollingDockDoorTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingDockDoorTemplateStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingDockDoorTemplateStatus): Boolean {
        when (status) {
            RollingDockDoorTemplateStatus.PLANNED -> {
                when (next) {
                    RollingDockDoorTemplateStatus.SCHEDULED, RollingDockDoorTemplateStatus.CANCELLED -> return true
                }
                return false
            }
            RollingDockDoorTemplateStatus.SCHEDULED -> {
                when (next) {
                    RollingDockDoorTemplateStatus.IN_PROGRESS, RollingDockDoorTemplateStatus.CANCELLED -> return true
                }
                return false
            }
            RollingDockDoorTemplateStatus.IN_PROGRESS -> return next == RollingDockDoorTemplateStatus.COMPLETED || next == RollingDockDoorTemplateStatus.CANCELLED
            RollingDockDoorTemplateStatus.COMPLETED -> return next == RollingDockDoorTemplateStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: RollingDockDoorTemplateStatus) {
        if (!canMoveTo(next)) {
            error("rolling dock door template $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun netLoad(): Double = round(floorValue * endsAt.toDouble() * 100) / 100

    fun snapshot(): RollingDockDoorTemplate {
        return RollingDockDoorTemplate(
            code = code,
            title = title,
            ratio = ratio,
            active = active,
            trailerId = trailerId,
            endsAt = endsAt,
            durationMinutes = durationMinutes,
            floorValue = floorValue,
            country = country,
            contactEmail = contactEmail,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a rolling dock door template in status planned. */
fun newRollingDockDoorTemplate(code: String, title: String): RollingDockDoorTemplate {
    return RollingDockDoorTemplate(
        code = code,
        title = title,
        ratio = 0.0,
        active = false,
        trailerId = "",
        endsAt = 0,
        durationMinutes = 0,
        floorValue = 0.0,
        country = "",
        contactEmail = "",
        pinned = false,
        status = RollingDockDoorTemplateStatus.PLANNED,
    )
}
