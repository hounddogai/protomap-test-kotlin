package com.acme.transport.network.node.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending node policy is in its life cycle. */
enum class PendingNodePolicyStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A pending node policy as the node team keeps it. */
data class PendingNodePolicy(
    var code: String,
    var title: String,
    var threshold: Double,
    var budget: Double,
    var attributes: MutableMap<String, String>,
    var endsAt: Int,
    var cost: Double,
    var loadKg: Double,
    var odometer: Int,
    var quantity: Int,
    var revision: Int,
    var status: PendingNodePolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingNodePolicyStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingNodePolicyStatus): Boolean {
        when (status) {
            PendingNodePolicyStatus.REQUESTED -> return next == PendingNodePolicyStatus.GRANTED
            PendingNodePolicyStatus.GRANTED -> return next == PendingNodePolicyStatus.EXPIRED
            PendingNodePolicyStatus.REVOKED -> {
                when (next) {
                    PendingNodePolicyStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingNodePolicyStatus) {
        if (!canMoveTo(next)) {
            error("pending node policy $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code: $title"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveValue(): Double = round((odometer.toDouble() + revision.toDouble()) * 100) / 100

    fun snapshot(): PendingNodePolicy {
        return PendingNodePolicy(
            code = code,
            title = title,
            threshold = threshold,
            budget = budget,
            attributes = attributes,
            endsAt = endsAt,
            cost = cost,
            loadKg = loadKg,
            odometer = odometer,
            quantity = quantity,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a pending node policy in status requested. */
fun newPendingNodePolicy(code: String, title: String): PendingNodePolicy {
    return PendingNodePolicy(
        code = code,
        title = title,
        threshold = 0.0,
        budget = 0.0,
        attributes = mutableMapOf<String, String>(),
        endsAt = 0,
        cost = 0.0,
        loadKg = 0.0,
        odometer = 0,
        quantity = 0,
        revision = 0,
        status = PendingNodePolicyStatus.REQUESTED,
    )
}
