package com.acme.transport.crewing.qualification.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The qualification template records of crewing's qualification work, and the rules, stores, and calculations
// around them.

/** Where a qualification template is in its life cycle. */
enum class QualificationTemplateStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A qualification template as the qualification team keeps it. */
data class QualificationTemplate(
    var code: String,
    var label: String,
    var floorValue: Double,
    var vehicleId: String,
    var unit: String,
    var durationMinutes: Int,
    var ownerId: String,
    var description: String,
    var phone: String,
    var name: String,
    var maximum: Double,
    var status: QualificationTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            QualificationTemplateStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: QualificationTemplateStatus): Boolean {
        when (status) {
            QualificationTemplateStatus.CREATED -> {
                when (next) {
                    QualificationTemplateStatus.PUBLISHED -> return true
                }
                return false
            }
            QualificationTemplateStatus.VALIDATED -> {
                when (next) {
                    QualificationTemplateStatus.RETIRED -> return true
                }
                return false
            }
            QualificationTemplateStatus.PUBLISHED -> return next == QualificationTemplateStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: QualificationTemplateStatus) {
        if (!canMoveTo(next)) {
            error("qualification template $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"qualification template"} $code"

    fun weightedValue(): Double = round(durationMinutes.toDouble() * floorValue * 100) / 100

    fun snapshot(): QualificationTemplate {
        return QualificationTemplate(
            code = code,
            label = label,
            floorValue = floorValue,
            vehicleId = vehicleId,
            unit = unit,
            durationMinutes = durationMinutes,
            ownerId = ownerId,
            description = description,
            phone = phone,
            name = name,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a qualification template in status created. */
fun newQualificationTemplate(code: String, label: String): QualificationTemplate {
    return QualificationTemplate(
        code = code,
        label = label,
        floorValue = 0.0,
        vehicleId = "",
        unit = "",
        durationMinutes = 0,
        ownerId = "",
        description = "",
        phone = "",
        name = "",
        maximum = 0.0,
        status = QualificationTemplateStatus.CREATED,
    )
}
