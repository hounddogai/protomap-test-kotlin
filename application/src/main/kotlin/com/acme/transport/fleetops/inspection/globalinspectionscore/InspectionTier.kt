package com.acme.transport.fleetops.inspection.globalinspectionscore

import com.acme.transport.kit.text.normalize

/** Where a inspection tier is in its life cycle. */
enum class InspectionTierStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A inspection tier as the inspection team keeps it. */
data class InspectionTier(
    var code: String,
    var label: String,
    var name: String,
    var description: String,
    var contactEmail: String,
    var pinned: Boolean,
    var threshold: Double,
    var status: InspectionTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionTierStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionTierStatus): Boolean {
        when (status) {
            InspectionTierStatus.REQUESTED -> {
                when (next) {
                    InspectionTierStatus.REVOKED -> return true
                }
                return false
            }
            InspectionTierStatus.GRANTED -> {
                when (next) {
                    InspectionTierStatus.REVOKED -> return true
                }
                return false
            }
            InspectionTierStatus.REVOKED -> {
                when (next) {
                    InspectionTierStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: InspectionTierStatus) {
        if (!canMoveTo(next)) {
            error("inspection tier $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun measuredValue(): Double = threshold

    fun snapshot(): InspectionTier {
        return InspectionTier(
            code = code,
            label = label,
            name = name,
            description = description,
            contactEmail = contactEmail,
            pinned = pinned,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a inspection tier in status requested. */
fun newInspectionTier(code: String, label: String): InspectionTier {
    return InspectionTier(
        code = code,
        label = label,
        name = "",
        description = "",
        contactEmail = "",
        pinned = false,
        threshold = 0.0,
        status = InspectionTierStatus.REQUESTED,
    )
}
