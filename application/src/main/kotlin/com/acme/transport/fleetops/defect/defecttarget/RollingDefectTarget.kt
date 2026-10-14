package com.acme.transport.fleetops.defect.defecttarget

import com.acme.transport.kit.text.normalize

// The rolling defect target records of fleetops's defect work, and the rules, stores, and calculations around
// them.

/** Where a rolling defect target is in its life cycle. */
enum class RollingDefectTargetStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A rolling defect target. Its code is unique within a tenant. */
data class RollingDefectTarget(
    var code: String,
    var title: String,
    var label: String,
    var endsAt: Int,
    var approved: Boolean,
    var country: String,
    var status: RollingDefectTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingDefectTargetStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingDefectTargetStatus): Boolean {
        when (status) {
            RollingDefectTargetStatus.IDLE -> {
                when (next) {
                    RollingDefectTargetStatus.READY, RollingDefectTargetStatus.STOPPED -> return true
                }
                return false
            }
            RollingDefectTargetStatus.WARMING -> {
                when (next) {
                    RollingDefectTargetStatus.STOPPED -> return true
                }
                return false
            }
            RollingDefectTargetStatus.READY -> {
                when (next) {
                    RollingDefectTargetStatus.STOPPED -> return true
                }
                return false
            }
            RollingDefectTargetStatus.DRAINING -> return next == RollingDefectTargetStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: RollingDefectTargetStatus) {
        if (!canMoveTo(next)) {
            error("rolling defect target $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $title"

    fun measuredValue(): Double = endsAt.toDouble()

    fun snapshot(): RollingDefectTarget {
        return RollingDefectTarget(
            code = code,
            title = title,
            label = label,
            endsAt = endsAt,
            approved = approved,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a rolling defect target in status idle. */
fun newRollingDefectTarget(code: String, title: String): RollingDefectTarget {
    return RollingDefectTarget(
        code = code,
        title = title,
        label = "",
        endsAt = 0,
        approved = false,
        country = "",
        status = RollingDefectTargetStatus.IDLE,
    )
}
