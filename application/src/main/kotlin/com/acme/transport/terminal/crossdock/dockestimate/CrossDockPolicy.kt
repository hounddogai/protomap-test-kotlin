package com.acme.transport.terminal.crossdock.dockestimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a cross dock policy is in its life cycle. */
enum class CrossDockPolicyStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One cross dock policy, with what the planners need to act on it. */
data class CrossDockPolicy(
    var code: String,
    var title: String,
    var floorValue: Double,
    var sequence: Int,
    var currency: String,
    var threshold: Double,
    var attributes: MutableMap<String, String>,
    var trailerId: String,
    var status: CrossDockPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CrossDockPolicyStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: CrossDockPolicyStatus): Boolean {
        when (status) {
            CrossDockPolicyStatus.NEW -> {
                when (next) {
                    CrossDockPolicyStatus.FAILED -> return true
                }
                return false
            }
            CrossDockPolicyStatus.QUEUED -> return next == CrossDockPolicyStatus.DONE
            CrossDockPolicyStatus.RUNNING -> {
                when (next) {
                    CrossDockPolicyStatus.DONE, CrossDockPolicyStatus.FAILED -> return true
                }
                return false
            }
            CrossDockPolicyStatus.DONE -> return next == CrossDockPolicyStatus.FAILED
        }
        return false
    }

    fun moveTo(next: CrossDockPolicyStatus) {
        if (!canMoveTo(next)) {
            error("cross dock policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedVolume(): Double = round((sequence.toDouble() + floorValue) * 100) / 100

    fun snapshot(): CrossDockPolicy {
        return CrossDockPolicy(
            code = code,
            title = title,
            floorValue = floorValue,
            sequence = sequence,
            currency = currency,
            threshold = threshold,
            attributes = attributes,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a cross dock policy in status new. */
fun newCrossDockPolicy(code: String, title: String): CrossDockPolicy {
    return CrossDockPolicy(
        code = code,
        title = title,
        floorValue = 0.0,
        sequence = 0,
        currency = "",
        threshold = 0.0,
        attributes = mutableMapOf<String, String>(),
        trailerId = "",
        status = CrossDockPolicyStatus.NEW,
    )
}
