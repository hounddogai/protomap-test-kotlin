package com.acme.transport.customs.clearance.clearancemanifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped clearance bundle is in its life cycle. */
enum class CappedClearanceBundleStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One capped clearance bundle, with what the planners need to act on it. */
data class CappedClearanceBundle(
    var code: String,
    var label: String,
    var durationMinutes: Int,
    var minimum: Double,
    var locale: String,
    var summary: String,
    var contactEmail: String,
    var channel: String,
    var routeCode: String,
    var category: String,
    var status: CappedClearanceBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedClearanceBundleStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedClearanceBundleStatus): Boolean {
        when (status) {
            CappedClearanceBundleStatus.CREATED -> {
                when (next) {
                    CappedClearanceBundleStatus.VALIDATED, CappedClearanceBundleStatus.RETIRED -> return true
                }
                return false
            }
            CappedClearanceBundleStatus.VALIDATED -> {
                when (next) {
                    CappedClearanceBundleStatus.RETIRED -> return true
                }
                return false
            }
            CappedClearanceBundleStatus.PUBLISHED -> {
                when (next) {
                    CappedClearanceBundleStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedClearanceBundleStatus) {
        if (!canMoveTo(next)) {
            error("capped clearance bundle $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun projectedExposure(): Double = round((minimum - durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): CappedClearanceBundle {
        return CappedClearanceBundle(
            code = code,
            label = label,
            durationMinutes = durationMinutes,
            minimum = minimum,
            locale = locale,
            summary = summary,
            contactEmail = contactEmail,
            channel = channel,
            routeCode = routeCode,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a capped clearance bundle in status created. */
fun newCappedClearanceBundle(code: String, label: String): CappedClearanceBundle {
    return CappedClearanceBundle(
        code = code,
        label = label,
        durationMinutes = 0,
        minimum = 0.0,
        locale = "",
        summary = "",
        contactEmail = "",
        channel = "",
        routeCode = "",
        category = "",
        status = CappedClearanceBundleStatus.CREATED,
    )
}
