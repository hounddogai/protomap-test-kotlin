package com.acme.transport.network.node.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped node variance is in its life cycle. */
enum class CappedNodeVarianceStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One capped node variance, with what the planners need to act on it. */
data class CappedNodeVariance(
    var code: String,
    var name: String,
    var retries: Int,
    var active: Boolean,
    var notes: String,
    var visible: Boolean,
    var threshold: Double,
    var score: Double,
    var pallets: Int,
    var graceDays: Int,
    var status: CappedNodeVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedNodeVarianceStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedNodeVarianceStatus): Boolean {
        when (status) {
            CappedNodeVarianceStatus.IDLE -> {
                when (next) {
                    CappedNodeVarianceStatus.READY, CappedNodeVarianceStatus.DRAINING -> return true
                }
                return false
            }
            CappedNodeVarianceStatus.WARMING -> {
                when (next) {
                    CappedNodeVarianceStatus.STOPPED -> return true
                }
                return false
            }
            CappedNodeVarianceStatus.READY -> return next == CappedNodeVarianceStatus.DRAINING || next == CappedNodeVarianceStatus.STOPPED
            CappedNodeVarianceStatus.DRAINING -> {
                when (next) {
                    CappedNodeVarianceStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedNodeVarianceStatus) {
        if (!canMoveTo(next)) {
            error("capped node variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun weightedValue(): Double = round((graceDays.toDouble() - retries.toDouble()) * 100) / 100

    fun snapshot(): CappedNodeVariance {
        return CappedNodeVariance(
            code = code,
            name = name,
            retries = retries,
            active = active,
            notes = notes,
            visible = visible,
            threshold = threshold,
            score = score,
            pallets = pallets,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a capped node variance in status idle. */
fun newCappedNodeVariance(code: String, name: String): CappedNodeVariance {
    return CappedNodeVariance(
        code = code,
        name = name,
        retries = 0,
        active = false,
        notes = "",
        visible = false,
        threshold = 0.0,
        score = 0.0,
        pallets = 0,
        graceDays = 0,
        status = CappedNodeVarianceStatus.IDLE,
    )
}
