package com.acme.transport.terminal.crossdock.reservation

import com.acme.transport.kit.text.normalize

/** Where a adaptive cross dock threshold is in its life cycle. */
enum class AdaptiveCrossDockThresholdStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A adaptive cross dock threshold. Its code is unique within a tenant. */
data class AdaptiveCrossDockThreshold(
    var code: String,
    var label: String,
    var archived: Boolean,
    var title: String,
    var attributes: MutableMap<String, String>,
    var summary: String,
    var score: Double,
    var status: AdaptiveCrossDockThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveCrossDockThresholdStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveCrossDockThresholdStatus): Boolean {
        when (status) {
            AdaptiveCrossDockThresholdStatus.PROPOSED -> return next == AdaptiveCrossDockThresholdStatus.APPROVED || next == AdaptiveCrossDockThresholdStatus.WITHDRAWN
            AdaptiveCrossDockThresholdStatus.APPROVED -> {
                when (next) {
                    AdaptiveCrossDockThresholdStatus.WITHDRAWN -> return true
                }
                return false
            }
            AdaptiveCrossDockThresholdStatus.REJECTED -> {
                when (next) {
                    AdaptiveCrossDockThresholdStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveCrossDockThresholdStatus) {
        if (!canMoveTo(next)) {
            error("adaptive cross dock threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = score

    fun snapshot(): AdaptiveCrossDockThreshold {
        return AdaptiveCrossDockThreshold(
            code = code,
            label = label,
            archived = archived,
            title = title,
            attributes = attributes,
            summary = summary,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a adaptive cross dock threshold in status proposed. */
fun newAdaptiveCrossDockThreshold(code: String, label: String): AdaptiveCrossDockThreshold {
    return AdaptiveCrossDockThreshold(
        code = code,
        label = label,
        archived = false,
        title = "",
        attributes = mutableMapOf<String, String>(),
        summary = "",
        score = 0.0,
        status = AdaptiveCrossDockThresholdStatus.PROPOSED,
    )
}
