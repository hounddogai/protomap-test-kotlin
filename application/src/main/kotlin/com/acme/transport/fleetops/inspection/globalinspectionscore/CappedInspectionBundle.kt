package com.acme.transport.fleetops.inspection.globalinspectionscore

import com.acme.transport.kit.text.normalize

/** Where a capped inspection bundle is in its life cycle. */
enum class CappedInspectionBundleStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One capped inspection bundle, with what the planners need to act on it. */
data class CappedInspectionBundle(
    var code: String,
    var label: String,
    var trailerId: String,
    var unit: String,
    var dock: String,
    var quantity: Int,
    var status: CappedInspectionBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedInspectionBundleStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedInspectionBundleStatus): Boolean {
        when (status) {
            CappedInspectionBundleStatus.OPEN -> {
                when (next) {
                    CappedInspectionBundleStatus.REOPENED, CappedInspectionBundleStatus.ARCHIVED -> return true
                }
                return false
            }
            CappedInspectionBundleStatus.IN_REVIEW -> {
                when (next) {
                    CappedInspectionBundleStatus.REOPENED -> return true
                }
                return false
            }
            CappedInspectionBundleStatus.RESOLVED -> {
                when (next) {
                    CappedInspectionBundleStatus.REOPENED, CappedInspectionBundleStatus.ARCHIVED -> return true
                }
                return false
            }
            CappedInspectionBundleStatus.REOPENED -> {
                when (next) {
                    CappedInspectionBundleStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedInspectionBundleStatus) {
        if (!canMoveTo(next)) {
            error("capped inspection bundle $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): CappedInspectionBundle {
        return CappedInspectionBundle(
            code = code,
            label = label,
            trailerId = trailerId,
            unit = unit,
            dock = dock,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a capped inspection bundle in status open. */
fun newCappedInspectionBundle(code: String, label: String): CappedInspectionBundle {
    return CappedInspectionBundle(
        code = code,
        label = label,
        trailerId = "",
        unit = "",
        dock = "",
        quantity = 0,
        status = CappedInspectionBundleStatus.OPEN,
    )
}
