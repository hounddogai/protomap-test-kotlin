package com.acme.transport.customs.declaration.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The declaration metric records of customs's declaration work, and the rules, stores, and calculations
// around them.

/** Where a declaration metric is in its life cycle. */
enum class DeclarationMetricStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A declaration metric as the declaration team keeps it. */
data class DeclarationMetric(
    var code: String,
    var title: String,
    var threshold: Double,
    var distanceKm: Double,
    var updatedAt: Int,
    var ratio: Double,
    var batchSize: Int,
    var status: DeclarationMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DeclarationMetricStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DeclarationMetricStatus): Boolean {
        when (status) {
            DeclarationMetricStatus.CREATED -> return next == DeclarationMetricStatus.RETIRED
            DeclarationMetricStatus.VALIDATED -> return next == DeclarationMetricStatus.RETIRED
            DeclarationMetricStatus.PUBLISHED -> {
                when (next) {
                    DeclarationMetricStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DeclarationMetricStatus) {
        if (!canMoveTo(next)) {
            error("declaration metric $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun weightedTotal(): Double = round((batchSize.toDouble() + ratio) * 100) / 100

    fun snapshot(): DeclarationMetric {
        return DeclarationMetric(
            code = code,
            title = title,
            threshold = threshold,
            distanceKm = distanceKm,
            updatedAt = updatedAt,
            ratio = ratio,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a declaration metric in status created. */
fun newDeclarationMetric(code: String, title: String): DeclarationMetric {
    return DeclarationMetric(
        code = code,
        title = title,
        threshold = 0.0,
        distanceKm = 0.0,
        updatedAt = 0,
        ratio = 0.0,
        batchSize = 0,
        status = DeclarationMetricStatus.CREATED,
    )
}
