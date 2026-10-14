package com.acme.transport.linehaul.relay.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The relay adjustment records of linehaul's relay work, and the rules, stores, and calculations around them.

/** Where a relay adjustment is in its life cycle. */
enum class RelayAdjustmentStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A relay adjustment as the relay team keeps it. */
data class RelayAdjustment(
    var code: String,
    var title: String,
    var source: String,
    var threshold: Double,
    var minimum: Double,
    var batchSize: Int,
    var score: Double,
    var updatedAt: Int,
    var locale: String,
    var maximum: Double,
    var status: RelayAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RelayAdjustmentStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RelayAdjustmentStatus): Boolean {
        when (status) {
            RelayAdjustmentStatus.CREATED -> return next == RelayAdjustmentStatus.VALIDATED || next == RelayAdjustmentStatus.RETIRED
            RelayAdjustmentStatus.VALIDATED -> return next == RelayAdjustmentStatus.PUBLISHED || next == RelayAdjustmentStatus.RETIRED
            RelayAdjustmentStatus.PUBLISHED -> return next == RelayAdjustmentStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: RelayAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("relay adjustment $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun weightedTotal(): Double = round((threshold + batchSize.toDouble()) * 100) / 100

    fun snapshot(): RelayAdjustment {
        return RelayAdjustment(
            code = code,
            title = title,
            source = source,
            threshold = threshold,
            minimum = minimum,
            batchSize = batchSize,
            score = score,
            updatedAt = updatedAt,
            locale = locale,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a relay adjustment in status created. */
fun newRelayAdjustment(code: String, title: String): RelayAdjustment {
    return RelayAdjustment(
        code = code,
        title = title,
        source = "",
        threshold = 0.0,
        minimum = 0.0,
        batchSize = 0,
        score = 0.0,
        updatedAt = 0,
        locale = "",
        maximum = 0.0,
        status = RelayAdjustmentStatus.CREATED,
    )
}
