package com.acme.transport.fleetops.inspection.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The inspection template records of fleetops's inspection work, and the rules, stores, and calculations
// around them.

/** Where a inspection template is in its life cycle. */
enum class InspectionTemplateStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One inspection template, with what the planners need to act on it. */
data class InspectionTemplate(
    var code: String,
    var name: String,
    var active: Boolean,
    var durationMinutes: Int,
    var discount: Double,
    var endsAt: Int,
    var createdAt: Int,
    var label: String,
    var phone: String,
    var status: InspectionTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionTemplateStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionTemplateStatus): Boolean {
        when (status) {
            InspectionTemplateStatus.REQUESTED -> {
                when (next) {
                    InspectionTemplateStatus.GRANTED, InspectionTemplateStatus.EXPIRED -> return true
                }
                return false
            }
            InspectionTemplateStatus.GRANTED -> {
                when (next) {
                    InspectionTemplateStatus.REVOKED, InspectionTemplateStatus.EXPIRED -> return true
                }
                return false
            }
            InspectionTemplateStatus.REVOKED -> {
                when (next) {
                    InspectionTemplateStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: InspectionTemplateStatus) {
        if (!canMoveTo(next)) {
            error("inspection template $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $name"

    fun netVolume(): Double = round(createdAt.toDouble() * endsAt.toDouble() * 100) / 100

    fun snapshot(): InspectionTemplate {
        return InspectionTemplate(
            code = code,
            name = name,
            active = active,
            durationMinutes = durationMinutes,
            discount = discount,
            endsAt = endsAt,
            createdAt = createdAt,
            label = label,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a inspection template in status requested. */
fun newInspectionTemplate(code: String, name: String): InspectionTemplate {
    return InspectionTemplate(
        code = code,
        name = name,
        active = false,
        durationMinutes = 0,
        discount = 0.0,
        endsAt = 0,
        createdAt = 0,
        label = "",
        phone = "",
        status = InspectionTemplateStatus.REQUESTED,
    )
}
