package com.acme.transport.safety.violation.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling violation window is in its life cycle. */
enum class RollingViolationWindowStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A rolling violation window. Its code is unique within a tenant. */
data class RollingViolationWindow(
    var code: String,
    var name: String,
    var archived: Boolean,
    var ratio: Double,
    var expiresAt: Int,
    var region: String,
    var active: Boolean,
    var floorValue: Double,
    var status: RollingViolationWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingViolationWindowStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingViolationWindowStatus): Boolean {
        when (status) {
            RollingViolationWindowStatus.CREATED -> {
                when (next) {
                    RollingViolationWindowStatus.VALIDATED -> return true
                }
                return false
            }
            RollingViolationWindowStatus.VALIDATED -> return next == RollingViolationWindowStatus.PUBLISHED || next == RollingViolationWindowStatus.RETIRED
            RollingViolationWindowStatus.PUBLISHED -> return next == RollingViolationWindowStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: RollingViolationWindowStatus) {
        if (!canMoveTo(next)) {
            error("rolling violation window $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun adjustedVolume(): Double = round(ratio * floorValue * 100) / 100

    fun snapshot(): RollingViolationWindow {
        return RollingViolationWindow(
            code = code,
            name = name,
            archived = archived,
            ratio = ratio,
            expiresAt = expiresAt,
            region = region,
            active = active,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a rolling violation window in status created. */
fun newRollingViolationWindow(code: String, name: String): RollingViolationWindow {
    return RollingViolationWindow(
        code = code,
        name = name,
        archived = false,
        ratio = 0.0,
        expiresAt = 0,
        region = "",
        active = false,
        floorValue = 0.0,
        status = RollingViolationWindowStatus.CREATED,
    )
}
