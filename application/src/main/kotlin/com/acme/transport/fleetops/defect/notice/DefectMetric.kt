package com.acme.transport.fleetops.defect.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a defect metric is in its life cycle. */
enum class DefectMetricStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A defect metric as the defect team keeps it. */
data class DefectMetric(
    var code: String,
    var name: String,
    var country: String,
    var unit: String,
    var discount: Double,
    var channel: String,
    var carrier: String,
    var createdAt: Int,
    var durationMinutes: Int,
    var tags: MutableList<String>,
    var status: DefectMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefectMetricStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefectMetricStatus): Boolean {
        when (status) {
            DefectMetricStatus.CREATED -> {
                when (next) {
                    DefectMetricStatus.RETIRED -> return true
                }
                return false
            }
            DefectMetricStatus.VALIDATED -> {
                when (next) {
                    DefectMetricStatus.PUBLISHED -> return true
                }
                return false
            }
            DefectMetricStatus.PUBLISHED -> return next == DefectMetricStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: DefectMetricStatus) {
        if (!canMoveTo(next)) {
            error("defect metric $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveLoad(): Double = round((createdAt.toDouble() + discount) * 100) / 100

    fun snapshot(): DefectMetric {
        return DefectMetric(
            code = code,
            name = name,
            country = country,
            unit = unit,
            discount = discount,
            channel = channel,
            carrier = carrier,
            createdAt = createdAt,
            durationMinutes = durationMinutes,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a defect metric in status created. */
fun newDefectMetric(code: String, name: String): DefectMetric {
    return DefectMetric(
        code = code,
        name = name,
        country = "",
        unit = "",
        discount = 0.0,
        channel = "",
        carrier = "",
        createdAt = 0,
        durationMinutes = 0,
        tags = mutableListOf<String>(),
        status = DefectMetricStatus.CREATED,
    )
}
