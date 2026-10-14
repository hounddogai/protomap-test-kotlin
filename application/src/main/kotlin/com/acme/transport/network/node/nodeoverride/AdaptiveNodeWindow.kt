package com.acme.transport.network.node.nodeoverride

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive node window is in its life cycle. */
enum class AdaptiveNodeWindowStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One adaptive node window, with what the planners need to act on it. */
data class AdaptiveNodeWindow(
    var code: String,
    var label: String,
    var visible: Boolean,
    var budget: Double,
    var score: Double,
    var routeCode: String,
    var description: String,
    var notes: String,
    var status: AdaptiveNodeWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveNodeWindowStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveNodeWindowStatus): Boolean {
        when (status) {
            AdaptiveNodeWindowStatus.PLANNED -> {
                when (next) {
                    AdaptiveNodeWindowStatus.IN_PROGRESS, AdaptiveNodeWindowStatus.COMPLETED -> return true
                }
                return false
            }
            AdaptiveNodeWindowStatus.SCHEDULED -> {
                when (next) {
                    AdaptiveNodeWindowStatus.CANCELLED -> return true
                }
                return false
            }
            AdaptiveNodeWindowStatus.IN_PROGRESS -> {
                when (next) {
                    AdaptiveNodeWindowStatus.CANCELLED -> return true
                }
                return false
            }
            AdaptiveNodeWindowStatus.COMPLETED -> {
                when (next) {
                    AdaptiveNodeWindowStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveNodeWindowStatus) {
        if (!canMoveTo(next)) {
            error("adaptive node window $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"adaptive node window"} $code"

    fun projectedVolume(): Double = round(budget * score * 100) / 100

    fun snapshot(): AdaptiveNodeWindow {
        return AdaptiveNodeWindow(
            code = code,
            label = label,
            visible = visible,
            budget = budget,
            score = score,
            routeCode = routeCode,
            description = description,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a adaptive node window in status planned. */
fun newAdaptiveNodeWindow(code: String, label: String): AdaptiveNodeWindow {
    return AdaptiveNodeWindow(
        code = code,
        label = label,
        visible = false,
        budget = 0.0,
        score = 0.0,
        routeCode = "",
        description = "",
        notes = "",
        status = AdaptiveNodeWindowStatus.PLANNED,
    )
}
