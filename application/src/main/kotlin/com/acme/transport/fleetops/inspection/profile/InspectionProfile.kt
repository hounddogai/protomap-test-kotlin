package com.acme.transport.fleetops.inspection.profile

import com.acme.transport.kit.text.normalize

// The inspection profile records of fleetops's inspection work, and the rules, stores, and calculations
// around them.

/** Where a inspection profile is in its life cycle. */
enum class InspectionProfileStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A inspection profile as the inspection team keeps it. */
data class InspectionProfile(
    var code: String,
    var title: String,
    var dock: String,
    var pallets: Int,
    var label: String,
    var status: InspectionProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionProfileStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionProfileStatus): Boolean {
        when (status) {
            InspectionProfileStatus.CREATED -> {
                when (next) {
                    InspectionProfileStatus.VALIDATED, InspectionProfileStatus.RETIRED -> return true
                }
                return false
            }
            InspectionProfileStatus.VALIDATED -> {
                when (next) {
                    InspectionProfileStatus.PUBLISHED, InspectionProfileStatus.RETIRED -> return true
                }
                return false
            }
            InspectionProfileStatus.PUBLISHED -> {
                when (next) {
                    InspectionProfileStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: InspectionProfileStatus) {
        if (!canMoveTo(next)) {
            error("inspection profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"inspection profile"} $code"

    fun measuredValue(): Double = pallets.toDouble()

    fun snapshot(): InspectionProfile {
        return InspectionProfile(
            code = code,
            title = title,
            dock = dock,
            pallets = pallets,
            label = label,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a inspection profile in status created. */
fun newInspectionProfile(code: String, title: String): InspectionProfile {
    return InspectionProfile(
        code = code,
        title = title,
        dock = "",
        pallets = 0,
        label = "",
        status = InspectionProfileStatus.CREATED,
    )
}
