package com.acme.transport.fleetops.defect.notice

import com.acme.transport.kit.text.normalize

/** Where a regional defect bundle is in its life cycle. */
enum class RegionalDefectBundleStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a regional defect bundle and its current status. */
data class RegionalDefectBundle(
    var code: String,
    var label: String,
    var windowDays: Int,
    var carrier: String,
    var tags: MutableList<String>,
    var driverId: String,
    var status: RegionalDefectBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalDefectBundleStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalDefectBundleStatus): Boolean {
        when (status) {
            RegionalDefectBundleStatus.OPEN -> return next == RegionalDefectBundleStatus.ARCHIVED
            RegionalDefectBundleStatus.IN_REVIEW -> return next == RegionalDefectBundleStatus.RESOLVED || next == RegionalDefectBundleStatus.REOPENED
            RegionalDefectBundleStatus.RESOLVED -> {
                when (next) {
                    RegionalDefectBundleStatus.REOPENED, RegionalDefectBundleStatus.ARCHIVED -> return true
                }
                return false
            }
            RegionalDefectBundleStatus.REOPENED -> return next == RegionalDefectBundleStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: RegionalDefectBundleStatus) {
        if (!canMoveTo(next)) {
            error("regional defect bundle $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = windowDays.toDouble()

    fun snapshot(): RegionalDefectBundle {
        return RegionalDefectBundle(
            code = code,
            label = label,
            windowDays = windowDays,
            carrier = carrier,
            tags = tags,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a regional defect bundle in status open. */
fun newRegionalDefectBundle(code: String, label: String): RegionalDefectBundle {
    return RegionalDefectBundle(
        code = code,
        label = label,
        windowDays = 0,
        carrier = "",
        tags = mutableListOf<String>(),
        driverId = "",
        status = RegionalDefectBundleStatus.OPEN,
    )
}
