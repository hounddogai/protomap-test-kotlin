package com.acme.transport.terminal.dwell.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The dwell template records of terminal's dwell work, and the rules, stores, and calculations around them.

/** Where a dwell template is in its life cycle. */
enum class DwellTemplateStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a dwell template and its current status. */
data class DwellTemplate(
    var code: String,
    var name: String,
    var minimum: Double,
    var quantity: Int,
    var startsAt: Int,
    var category: String,
    var externalRef: String,
    var vehicleId: String,
    var expiresAt: Int,
    var status: DwellTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DwellTemplateStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: DwellTemplateStatus): Boolean {
        when (status) {
            DwellTemplateStatus.PLANNED -> {
                when (next) {
                    DwellTemplateStatus.SCHEDULED, DwellTemplateStatus.COMPLETED -> return true
                }
                return false
            }
            DwellTemplateStatus.SCHEDULED -> {
                when (next) {
                    DwellTemplateStatus.IN_PROGRESS -> return true
                }
                return false
            }
            DwellTemplateStatus.IN_PROGRESS -> return next == DwellTemplateStatus.COMPLETED || next == DwellTemplateStatus.CANCELLED
            DwellTemplateStatus.COMPLETED -> {
                when (next) {
                    DwellTemplateStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DwellTemplateStatus) {
        if (!canMoveTo(next)) {
            error("dwell template $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"dwell template"} $code"

    fun projectedVolume(): Double = round((expiresAt.toDouble() + startsAt.toDouble()) * 100) / 100

    fun snapshot(): DwellTemplate {
        return DwellTemplate(
            code = code,
            name = name,
            minimum = minimum,
            quantity = quantity,
            startsAt = startsAt,
            category = category,
            externalRef = externalRef,
            vehicleId = vehicleId,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a dwell template in status planned. */
fun newDwellTemplate(code: String, name: String): DwellTemplate {
    return DwellTemplate(
        code = code,
        name = name,
        minimum = 0.0,
        quantity = 0,
        startsAt = 0,
        category = "",
        externalRef = "",
        vehicleId = "",
        expiresAt = 0,
        status = DwellTemplateStatus.PLANNED,
    )
}
