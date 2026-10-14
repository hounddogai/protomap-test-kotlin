package com.acme.transport.crewing.qualification.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional qualification window is in its life cycle. */
enum class RegionalQualificationWindowStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One regional qualification window, with what the planners need to act on it. */
data class RegionalQualificationWindow(
    var code: String,
    var name: String,
    var etaMinutes: Int,
    var windowDays: Int,
    var pallets: Int,
    var updatedAt: Int,
    var expiresAt: Int,
    var status: RegionalQualificationWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalQualificationWindowStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalQualificationWindowStatus): Boolean {
        when (status) {
            RegionalQualificationWindowStatus.PROPOSED -> return next == RegionalQualificationWindowStatus.WITHDRAWN
            RegionalQualificationWindowStatus.APPROVED -> return next == RegionalQualificationWindowStatus.REJECTED
            RegionalQualificationWindowStatus.REJECTED -> return next == RegionalQualificationWindowStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: RegionalQualificationWindowStatus) {
        if (!canMoveTo(next)) {
            error("regional qualification window $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun weightedTotal(): Double = round(etaMinutes.toDouble() * pallets.toDouble() * 100) / 100

    fun snapshot(): RegionalQualificationWindow {
        return RegionalQualificationWindow(
            code = code,
            name = name,
            etaMinutes = etaMinutes,
            windowDays = windowDays,
            pallets = pallets,
            updatedAt = updatedAt,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a regional qualification window in status proposed. */
fun newRegionalQualificationWindow(code: String, name: String): RegionalQualificationWindow {
    return RegionalQualificationWindow(
        code = code,
        name = name,
        etaMinutes = 0,
        windowDays = 0,
        pallets = 0,
        updatedAt = 0,
        expiresAt = 0,
        status = RegionalQualificationWindowStatus.PROPOSED,
    )
}
