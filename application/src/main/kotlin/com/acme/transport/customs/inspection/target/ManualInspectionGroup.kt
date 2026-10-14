package com.acme.transport.customs.inspection.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual inspection group is in its life cycle. */
enum class ManualInspectionGroupStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One manual inspection group, with what the planners need to act on it. */
data class ManualInspectionGroup(
    var code: String,
    var title: String,
    var tags: MutableList<String>,
    var pinned: Boolean,
    var segment: String,
    var approved: Boolean,
    var carrier: String,
    var odometer: Int,
    var graceDays: Int,
    var status: ManualInspectionGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualInspectionGroupStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualInspectionGroupStatus): Boolean {
        when (status) {
            ManualInspectionGroupStatus.DRAFT -> {
                when (next) {
                    ManualInspectionGroupStatus.SUSPENDED, ManualInspectionGroupStatus.CLOSED -> return true
                }
                return false
            }
            ManualInspectionGroupStatus.PENDING -> return next == ManualInspectionGroupStatus.SUSPENDED || next == ManualInspectionGroupStatus.CLOSED
            ManualInspectionGroupStatus.ACTIVE -> {
                when (next) {
                    ManualInspectionGroupStatus.SUSPENDED -> return true
                }
                return false
            }
            ManualInspectionGroupStatus.SUSPENDED -> return next == ManualInspectionGroupStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: ManualInspectionGroupStatus) {
        if (!canMoveTo(next)) {
            error("manual inspection group $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveLoad(): Double = round((graceDays.toDouble() + odometer.toDouble()) * 100) / 100

    fun snapshot(): ManualInspectionGroup {
        return ManualInspectionGroup(
            code = code,
            title = title,
            tags = tags,
            pinned = pinned,
            segment = segment,
            approved = approved,
            carrier = carrier,
            odometer = odometer,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a manual inspection group in status draft. */
fun newManualInspectionGroup(code: String, title: String): ManualInspectionGroup {
    return ManualInspectionGroup(
        code = code,
        title = title,
        tags = mutableListOf<String>(),
        pinned = false,
        segment = "",
        approved = false,
        carrier = "",
        odometer = 0,
        graceDays = 0,
        status = ManualInspectionGroupStatus.DRAFT,
    )
}
