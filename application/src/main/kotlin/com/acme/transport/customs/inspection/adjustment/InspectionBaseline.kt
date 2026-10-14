package com.acme.transport.customs.inspection.adjustment

import com.acme.transport.kit.text.normalize

/** Where a inspection baseline is in its life cycle. */
enum class InspectionBaselineStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A inspection baseline as the inspection team keeps it. */
data class InspectionBaseline(
    var code: String,
    var label: String,
    var attributes: MutableMap<String, String>,
    var approved: Boolean,
    var routeCode: String,
    var visible: Boolean,
    var quantity: Int,
    var status: InspectionBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionBaselineStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionBaselineStatus): Boolean {
        when (status) {
            InspectionBaselineStatus.REQUESTED -> {
                when (next) {
                    InspectionBaselineStatus.GRANTED, InspectionBaselineStatus.EXPIRED -> return true
                }
                return false
            }
            InspectionBaselineStatus.GRANTED -> return next == InspectionBaselineStatus.EXPIRED
            InspectionBaselineStatus.REVOKED -> {
                when (next) {
                    InspectionBaselineStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: InspectionBaselineStatus) {
        if (!canMoveTo(next)) {
            error("inspection baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): InspectionBaseline {
        return InspectionBaseline(
            code = code,
            label = label,
            attributes = attributes,
            approved = approved,
            routeCode = routeCode,
            visible = visible,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a inspection baseline in status requested. */
fun newInspectionBaseline(code: String, label: String): InspectionBaseline {
    return InspectionBaseline(
        code = code,
        label = label,
        attributes = mutableMapOf<String, String>(),
        approved = false,
        routeCode = "",
        visible = false,
        quantity = 0,
        status = InspectionBaselineStatus.REQUESTED,
    )
}
