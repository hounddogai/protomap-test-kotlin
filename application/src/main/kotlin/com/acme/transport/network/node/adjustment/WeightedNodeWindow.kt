package com.acme.transport.network.node.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted node window is in its life cycle. */
enum class WeightedNodeWindowStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A weighted node window. Its code is unique within a tenant. */
data class WeightedNodeWindow(
    var code: String,
    var name: String,
    var discount: Double,
    var revision: Int,
    var threshold: Double,
    var rate: Double,
    var createdAt: Int,
    var segment: String,
    var status: WeightedNodeWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedNodeWindowStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedNodeWindowStatus): Boolean {
        when (status) {
            WeightedNodeWindowStatus.REQUESTED -> return next == WeightedNodeWindowStatus.REVOKED || next == WeightedNodeWindowStatus.EXPIRED
            WeightedNodeWindowStatus.GRANTED -> {
                when (next) {
                    WeightedNodeWindowStatus.REVOKED, WeightedNodeWindowStatus.EXPIRED -> return true
                }
                return false
            }
            WeightedNodeWindowStatus.REVOKED -> {
                when (next) {
                    WeightedNodeWindowStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedNodeWindowStatus) {
        if (!canMoveTo(next)) {
            error("weighted node window $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code / $name"

    fun netValue(): Double = round(discount * revision.toDouble() * 100) / 100

    fun snapshot(): WeightedNodeWindow {
        return WeightedNodeWindow(
            code = code,
            name = name,
            discount = discount,
            revision = revision,
            threshold = threshold,
            rate = rate,
            createdAt = createdAt,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a weighted node window in status requested. */
fun newWeightedNodeWindow(code: String, name: String): WeightedNodeWindow {
    return WeightedNodeWindow(
        code = code,
        name = name,
        discount = 0.0,
        revision = 0,
        threshold = 0.0,
        rate = 0.0,
        createdAt = 0,
        segment = "",
        status = WeightedNodeWindowStatus.REQUESTED,
    )
}
