package com.acme.transport.fleetops.tire.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tire template is in its life cycle. */
enum class TireTemplateStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a tire template and its current status. */
data class TireTemplate(
    var code: String,
    var name: String,
    var capacity: Int,
    var description: String,
    var startsAt: Int,
    var status: TireTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TireTemplateStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TireTemplateStatus): Boolean {
        when (status) {
            TireTemplateStatus.REQUESTED -> return next == TireTemplateStatus.GRANTED || next == TireTemplateStatus.REVOKED
            TireTemplateStatus.GRANTED -> return next == TireTemplateStatus.REVOKED
            TireTemplateStatus.REVOKED -> {
                when (next) {
                    TireTemplateStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TireTemplateStatus) {
        if (!canMoveTo(next)) {
            error("tire template $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun projectedVolume(): Double = round(capacity.toDouble() * startsAt.toDouble() * 100) / 100

    fun snapshot(): TireTemplate {
        return TireTemplate(
            code = code,
            name = name,
            capacity = capacity,
            description = description,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a tire template in status requested. */
fun newTireTemplate(code: String, name: String): TireTemplate {
    return TireTemplate(
        code = code,
        name = name,
        capacity = 0,
        description = "",
        startsAt = 0,
        status = TireTemplateStatus.REQUESTED,
    )
}
