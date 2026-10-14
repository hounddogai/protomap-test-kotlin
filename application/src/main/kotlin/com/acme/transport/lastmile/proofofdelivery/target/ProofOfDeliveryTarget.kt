package com.acme.transport.lastmile.proofofdelivery.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The proof of delivery target records of lastmile's proof of delivery work, and the rules, stores, and
// calculations around them.

/** Where a proof of delivery target is in its life cycle. */
enum class ProofOfDeliveryTargetStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A proof of delivery target as the proof of delivery team keeps it. */
data class ProofOfDeliveryTarget(
    var code: String,
    var label: String,
    var depot: String,
    var attributes: MutableMap<String, String>,
    var retries: Int,
    var attempts: Int,
    var durationMinutes: Int,
    var status: ProofOfDeliveryTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ProofOfDeliveryTargetStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ProofOfDeliveryTargetStatus): Boolean {
        when (status) {
            ProofOfDeliveryTargetStatus.NEW -> {
                when (next) {
                    ProofOfDeliveryTargetStatus.QUEUED, ProofOfDeliveryTargetStatus.RUNNING -> return true
                }
                return false
            }
            ProofOfDeliveryTargetStatus.QUEUED -> return next == ProofOfDeliveryTargetStatus.DONE
            ProofOfDeliveryTargetStatus.RUNNING -> return next == ProofOfDeliveryTargetStatus.FAILED
            ProofOfDeliveryTargetStatus.DONE -> {
                when (next) {
                    ProofOfDeliveryTargetStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ProofOfDeliveryTargetStatus) {
        if (!canMoveTo(next)) {
            error("proof of delivery target $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"proof of delivery target"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveValue(): Double = round((retries.toDouble() - attempts.toDouble()) * 100) / 100

    fun snapshot(): ProofOfDeliveryTarget {
        return ProofOfDeliveryTarget(
            code = code,
            label = label,
            depot = depot,
            attributes = attributes,
            retries = retries,
            attempts = attempts,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a proof of delivery target in status new. */
fun newProofOfDeliveryTarget(code: String, label: String): ProofOfDeliveryTarget {
    return ProofOfDeliveryTarget(
        code = code,
        label = label,
        depot = "",
        attributes = mutableMapOf<String, String>(),
        retries = 0,
        attempts = 0,
        durationMinutes = 0,
        status = ProofOfDeliveryTargetStatus.NEW,
    )
}
