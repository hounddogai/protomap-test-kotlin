package com.acme.transport.lastmile.proofofdelivery.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The proof of delivery window records of lastmile's proof of delivery work, and the rules, stores, and
// calculations around them.

/** Where a proof of delivery window is in its life cycle. */
enum class ProofOfDeliveryWindowStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One proof of delivery window, with what the planners need to act on it. */
data class ProofOfDeliveryWindow(
    var code: String,
    var name: String,
    var version: Int,
    var country: String,
    var attributes: MutableMap<String, String>,
    var threshold: Double,
    var visible: Boolean,
    var depot: String,
    var durationMinutes: Int,
    var fuelLiters: Double,
    var status: ProofOfDeliveryWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ProofOfDeliveryWindowStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ProofOfDeliveryWindowStatus): Boolean {
        when (status) {
            ProofOfDeliveryWindowStatus.NEW -> return next == ProofOfDeliveryWindowStatus.FAILED
            ProofOfDeliveryWindowStatus.QUEUED -> return next == ProofOfDeliveryWindowStatus.RUNNING
            ProofOfDeliveryWindowStatus.RUNNING -> {
                when (next) {
                    ProofOfDeliveryWindowStatus.DONE -> return true
                }
                return false
            }
            ProofOfDeliveryWindowStatus.DONE -> {
                when (next) {
                    ProofOfDeliveryWindowStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ProofOfDeliveryWindowStatus) {
        if (!canMoveTo(next)) {
            error("proof of delivery window $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code / $name"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netExposure(): Double = round((version.toDouble() - fuelLiters) * 100) / 100

    fun snapshot(): ProofOfDeliveryWindow {
        return ProofOfDeliveryWindow(
            code = code,
            name = name,
            version = version,
            country = country,
            attributes = attributes,
            threshold = threshold,
            visible = visible,
            depot = depot,
            durationMinutes = durationMinutes,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a proof of delivery window in status new. */
fun newProofOfDeliveryWindow(code: String, name: String): ProofOfDeliveryWindow {
    return ProofOfDeliveryWindow(
        code = code,
        name = name,
        version = 0,
        country = "",
        attributes = mutableMapOf<String, String>(),
        threshold = 0.0,
        visible = false,
        depot = "",
        durationMinutes = 0,
        fuelLiters = 0.0,
        status = ProofOfDeliveryWindowStatus.NEW,
    )
}
