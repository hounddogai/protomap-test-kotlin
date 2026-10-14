package com.acme.transport.customs.inspection.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive inspection profile is in its life cycle. */
enum class AdaptiveInspectionProfileStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One adaptive inspection profile, with what the planners need to act on it. */
data class AdaptiveInspectionProfile(
    var code: String,
    var name: String,
    var ceiling: Double,
    var margin: Double,
    var visible: Boolean,
    var status: AdaptiveInspectionProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveInspectionProfileStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveInspectionProfileStatus): Boolean {
        when (status) {
            AdaptiveInspectionProfileStatus.DRAFT -> {
                when (next) {
                    AdaptiveInspectionProfileStatus.PENDING -> return true
                }
                return false
            }
            AdaptiveInspectionProfileStatus.PENDING -> return next == AdaptiveInspectionProfileStatus.ACTIVE || next == AdaptiveInspectionProfileStatus.SUSPENDED
            AdaptiveInspectionProfileStatus.ACTIVE -> {
                when (next) {
                    AdaptiveInspectionProfileStatus.CLOSED -> return true
                }
                return false
            }
            AdaptiveInspectionProfileStatus.SUSPENDED -> {
                when (next) {
                    AdaptiveInspectionProfileStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveInspectionProfileStatus) {
        if (!canMoveTo(next)) {
            error("adaptive inspection profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun adjustedLoad(): Double = round((ceiling + margin) * 100) / 100

    fun snapshot(): AdaptiveInspectionProfile {
        return AdaptiveInspectionProfile(
            code = code,
            name = name,
            ceiling = ceiling,
            margin = margin,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a adaptive inspection profile in status draft. */
fun newAdaptiveInspectionProfile(code: String, name: String): AdaptiveInspectionProfile {
    return AdaptiveInspectionProfile(
        code = code,
        name = name,
        ceiling = 0.0,
        margin = 0.0,
        visible = false,
        status = AdaptiveInspectionProfileStatus.DRAFT,
    )
}
