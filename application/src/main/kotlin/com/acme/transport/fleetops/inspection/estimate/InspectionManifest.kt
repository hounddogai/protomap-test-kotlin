package com.acme.transport.fleetops.inspection.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a inspection manifest is in its life cycle. */
enum class InspectionManifestStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A inspection manifest as the inspection team keeps it. */
data class InspectionManifest(
    var code: String,
    var title: String,
    var budget: Double,
    var amount: Double,
    var carrier: String,
    var status: InspectionManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionManifestStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionManifestStatus): Boolean {
        when (status) {
            InspectionManifestStatus.CREATED -> return next == InspectionManifestStatus.VALIDATED
            InspectionManifestStatus.VALIDATED -> {
                when (next) {
                    InspectionManifestStatus.RETIRED -> return true
                }
                return false
            }
            InspectionManifestStatus.PUBLISHED -> {
                when (next) {
                    InspectionManifestStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: InspectionManifestStatus) {
        if (!canMoveTo(next)) {
            error("inspection manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"inspection manifest"} $code"

    fun weightedExposure(): Double = round((budget - amount) * 100) / 100

    fun snapshot(): InspectionManifest {
        return InspectionManifest(
            code = code,
            title = title,
            budget = budget,
            amount = amount,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a inspection manifest in status created. */
fun newInspectionManifest(code: String, title: String): InspectionManifest {
    return InspectionManifest(
        code = code,
        title = title,
        budget = 0.0,
        amount = 0.0,
        carrier = "",
        status = InspectionManifestStatus.CREATED,
    )
}
