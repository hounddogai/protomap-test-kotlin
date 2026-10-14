package com.acme.transport.safety.violation.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped violation threshold is in its life cycle. */
enum class CappedViolationThresholdStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One capped violation threshold, with what the planners need to act on it. */
data class CappedViolationThreshold(
    var code: String,
    var title: String,
    var source: String,
    var threshold: Double,
    var carrier: String,
    var ratio: Double,
    var attributes: MutableMap<String, String>,
    var batchSize: Int,
    var quantity: Int,
    var status: CappedViolationThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedViolationThresholdStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedViolationThresholdStatus): Boolean {
        when (status) {
            CappedViolationThresholdStatus.IDLE -> return next == CappedViolationThresholdStatus.READY
            CappedViolationThresholdStatus.WARMING -> return next == CappedViolationThresholdStatus.READY || next == CappedViolationThresholdStatus.DRAINING
            CappedViolationThresholdStatus.READY -> return next == CappedViolationThresholdStatus.DRAINING || next == CappedViolationThresholdStatus.STOPPED
            CappedViolationThresholdStatus.DRAINING -> {
                when (next) {
                    CappedViolationThresholdStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedViolationThresholdStatus) {
        if (!canMoveTo(next)) {
            error("capped violation threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"capped violation threshold"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netVolume(): Double = round((ratio - batchSize.toDouble()) * 100) / 100

    fun snapshot(): CappedViolationThreshold {
        return CappedViolationThreshold(
            code = code,
            title = title,
            source = source,
            threshold = threshold,
            carrier = carrier,
            ratio = ratio,
            attributes = attributes,
            batchSize = batchSize,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a capped violation threshold in status idle. */
fun newCappedViolationThreshold(code: String, title: String): CappedViolationThreshold {
    return CappedViolationThreshold(
        code = code,
        title = title,
        source = "",
        threshold = 0.0,
        carrier = "",
        ratio = 0.0,
        attributes = mutableMapOf<String, String>(),
        batchSize = 0,
        quantity = 0,
        status = CappedViolationThresholdStatus.IDLE,
    )
}
