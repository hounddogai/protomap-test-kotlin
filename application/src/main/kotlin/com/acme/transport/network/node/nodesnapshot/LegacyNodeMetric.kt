package com.acme.transport.network.node.nodesnapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy node metric is in its life cycle. */
enum class LegacyNodeMetricStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A legacy node metric as the node team keeps it. */
data class LegacyNodeMetric(
    var code: String,
    var label: String,
    var phone: String,
    var margin: Double,
    var rate: Double,
    var quantity: Int,
    var status: LegacyNodeMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyNodeMetricStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyNodeMetricStatus): Boolean {
        when (status) {
            LegacyNodeMetricStatus.PLANNED -> {
                when (next) {
                    LegacyNodeMetricStatus.IN_PROGRESS -> return true
                }
                return false
            }
            LegacyNodeMetricStatus.SCHEDULED -> {
                when (next) {
                    LegacyNodeMetricStatus.COMPLETED -> return true
                }
                return false
            }
            LegacyNodeMetricStatus.IN_PROGRESS -> {
                when (next) {
                    LegacyNodeMetricStatus.CANCELLED -> return true
                }
                return false
            }
            LegacyNodeMetricStatus.COMPLETED -> {
                when (next) {
                    LegacyNodeMetricStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyNodeMetricStatus) {
        if (!canMoveTo(next)) {
            error("legacy node metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"legacy node metric"} $code"

    fun weightedTotal(): Double = round((rate + quantity.toDouble()) * 100) / 100

    fun snapshot(): LegacyNodeMetric {
        return LegacyNodeMetric(
            code = code,
            label = label,
            phone = phone,
            margin = margin,
            rate = rate,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a legacy node metric in status planned. */
fun newLegacyNodeMetric(code: String, label: String): LegacyNodeMetric {
    return LegacyNodeMetric(
        code = code,
        label = label,
        phone = "",
        margin = 0.0,
        rate = 0.0,
        quantity = 0,
        status = LegacyNodeMetricStatus.PLANNED,
    )
}
