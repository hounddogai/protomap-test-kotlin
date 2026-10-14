package com.acme.transport.customs.inspection.pendinginspectionrule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual inspection template is in its life cycle. */
enum class ManualInspectionTemplateStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A manual inspection template. Its code is unique within a tenant. */
data class ManualInspectionTemplate(
    var code: String,
    var title: String,
    var odometer: Int,
    var floorValue: Double,
    var ownerId: String,
    var country: String,
    var status: ManualInspectionTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualInspectionTemplateStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualInspectionTemplateStatus): Boolean {
        when (status) {
            ManualInspectionTemplateStatus.REQUESTED -> {
                when (next) {
                    ManualInspectionTemplateStatus.GRANTED -> return true
                }
                return false
            }
            ManualInspectionTemplateStatus.GRANTED -> {
                when (next) {
                    ManualInspectionTemplateStatus.REVOKED -> return true
                }
                return false
            }
            ManualInspectionTemplateStatus.REVOKED -> return next == ManualInspectionTemplateStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: ManualInspectionTemplateStatus) {
        if (!canMoveTo(next)) {
            error("manual inspection template $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun adjustedExposure(): Double = round((floorValue + odometer.toDouble()) * 100) / 100

    fun snapshot(): ManualInspectionTemplate {
        return ManualInspectionTemplate(
            code = code,
            title = title,
            odometer = odometer,
            floorValue = floorValue,
            ownerId = ownerId,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a manual inspection template in status requested. */
fun newManualInspectionTemplate(code: String, title: String): ManualInspectionTemplate {
    return ManualInspectionTemplate(
        code = code,
        title = title,
        odometer = 0,
        floorValue = 0.0,
        ownerId = "",
        country = "",
        status = ManualInspectionTemplateStatus.REQUESTED,
    )
}
