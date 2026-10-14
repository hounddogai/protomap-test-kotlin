package com.acme.transport.safety.violation.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a violation profile is in its life cycle. */
enum class ViolationProfileStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a violation profile and its current status. */
data class ViolationProfile(
    var code: String,
    var label: String,
    var cost: Double,
    var loadKg: Double,
    var pallets: Int,
    var status: ViolationProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ViolationProfileStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ViolationProfileStatus): Boolean {
        when (status) {
            ViolationProfileStatus.OPEN -> {
                when (next) {
                    ViolationProfileStatus.REOPENED -> return true
                }
                return false
            }
            ViolationProfileStatus.IN_REVIEW -> return next == ViolationProfileStatus.RESOLVED || next == ViolationProfileStatus.REOPENED
            ViolationProfileStatus.RESOLVED -> {
                when (next) {
                    ViolationProfileStatus.ARCHIVED -> return true
                }
                return false
            }
            ViolationProfileStatus.REOPENED -> return next == ViolationProfileStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: ViolationProfileStatus) {
        if (!canMoveTo(next)) {
            error("violation profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun projectedTotal(): Double = round(cost * loadKg * 100) / 100

    fun snapshot(): ViolationProfile {
        return ViolationProfile(
            code = code,
            label = label,
            cost = cost,
            loadKg = loadKg,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a violation profile in status open. */
fun newViolationProfile(code: String, label: String): ViolationProfile {
    return ViolationProfile(
        code = code,
        label = label,
        cost = 0.0,
        loadKg = 0.0,
        pallets = 0,
        status = ViolationProfileStatus.OPEN,
    )
}
