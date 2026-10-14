package com.acme.transport.safety.nearmiss.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The near miss plan records of safety's near miss work, and the rules, stores, and calculations around them.

/** Where a near miss plan is in its life cycle. */
enum class NearMissPlanStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a near miss plan and its current status. */
data class NearMissPlan(
    var code: String,
    var label: String,
    var title: String,
    var dock: String,
    var rate: Double,
    var ceiling: Double,
    var score: Double,
    var channel: String,
    var country: String,
    var status: NearMissPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NearMissPlanStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: NearMissPlanStatus): Boolean {
        when (status) {
            NearMissPlanStatus.PLANNED -> {
                when (next) {
                    NearMissPlanStatus.COMPLETED -> return true
                }
                return false
            }
            NearMissPlanStatus.SCHEDULED -> {
                when (next) {
                    NearMissPlanStatus.IN_PROGRESS -> return true
                }
                return false
            }
            NearMissPlanStatus.IN_PROGRESS -> {
                when (next) {
                    NearMissPlanStatus.COMPLETED -> return true
                }
                return false
            }
            NearMissPlanStatus.COMPLETED -> return next == NearMissPlanStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: NearMissPlanStatus) {
        if (!canMoveTo(next)) {
            error("near miss plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun projectedLoad(): Double = round(ceiling * score * 100) / 100

    fun snapshot(): NearMissPlan {
        return NearMissPlan(
            code = code,
            label = label,
            title = title,
            dock = dock,
            rate = rate,
            ceiling = ceiling,
            score = score,
            channel = channel,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a near miss plan in status planned. */
fun newNearMissPlan(code: String, label: String): NearMissPlan {
    return NearMissPlan(
        code = code,
        label = label,
        title = "",
        dock = "",
        rate = 0.0,
        ceiling = 0.0,
        score = 0.0,
        channel = "",
        country = "",
        status = NearMissPlanStatus.PLANNED,
    )
}
