package com.acme.transport.lastmile.proofofdelivery.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The weighted proof of delivery manifest records of lastmile's proof of delivery work, and the rules,
// stores, and calculations around them.

/** Where a weighted proof of delivery manifest is in its life cycle. */
enum class WeightedProofOfDeliveryManifestStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A weighted proof of delivery manifest as the proof of delivery team keeps it. */
data class WeightedProofOfDeliveryManifest(
    var code: String,
    var name: String,
    var ceiling: Double,
    var reviewer: String,
    var tags: MutableList<String>,
    var dock: String,
    var priority: Int,
    var amount: Double,
    var status: WeightedProofOfDeliveryManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedProofOfDeliveryManifestStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedProofOfDeliveryManifestStatus): Boolean {
        when (status) {
            WeightedProofOfDeliveryManifestStatus.PROPOSED -> {
                when (next) {
                    WeightedProofOfDeliveryManifestStatus.APPROVED, WeightedProofOfDeliveryManifestStatus.REJECTED -> return true
                }
                return false
            }
            WeightedProofOfDeliveryManifestStatus.APPROVED -> return next == WeightedProofOfDeliveryManifestStatus.REJECTED || next == WeightedProofOfDeliveryManifestStatus.WITHDRAWN
            WeightedProofOfDeliveryManifestStatus.REJECTED -> {
                when (next) {
                    WeightedProofOfDeliveryManifestStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedProofOfDeliveryManifestStatus) {
        if (!canMoveTo(next)) {
            error("weighted proof of delivery manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveExposure(): Double = round((priority.toDouble() - amount) * 100) / 100

    fun snapshot(): WeightedProofOfDeliveryManifest {
        return WeightedProofOfDeliveryManifest(
            code = code,
            name = name,
            ceiling = ceiling,
            reviewer = reviewer,
            tags = tags,
            dock = dock,
            priority = priority,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a weighted proof of delivery manifest in status proposed. */
fun newWeightedProofOfDeliveryManifest(code: String, name: String): WeightedProofOfDeliveryManifest {
    return WeightedProofOfDeliveryManifest(
        code = code,
        name = name,
        ceiling = 0.0,
        reviewer = "",
        tags = mutableListOf<String>(),
        dock = "",
        priority = 0,
        amount = 0.0,
        status = WeightedProofOfDeliveryManifestStatus.PROPOSED,
    )
}
