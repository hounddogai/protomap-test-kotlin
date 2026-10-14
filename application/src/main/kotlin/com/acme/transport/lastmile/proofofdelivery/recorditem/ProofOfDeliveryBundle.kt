package com.acme.transport.lastmile.proofofdelivery.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a proof of delivery bundle is in its life cycle. */
enum class ProofOfDeliveryBundleStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A proof of delivery bundle. Its code is unique within a tenant. */
data class ProofOfDeliveryBundle(
    var code: String,
    var title: String,
    var depot: String,
    var source: String,
    var distanceKm: Double,
    var tags: MutableList<String>,
    var capacity: Int,
    var ratio: Double,
    var minimum: Double,
    var category: String,
    var status: ProofOfDeliveryBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ProofOfDeliveryBundleStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ProofOfDeliveryBundleStatus): Boolean {
        when (status) {
            ProofOfDeliveryBundleStatus.NEW -> return next == ProofOfDeliveryBundleStatus.DONE
            ProofOfDeliveryBundleStatus.QUEUED -> return next == ProofOfDeliveryBundleStatus.RUNNING || next == ProofOfDeliveryBundleStatus.FAILED
            ProofOfDeliveryBundleStatus.RUNNING -> return next == ProofOfDeliveryBundleStatus.DONE || next == ProofOfDeliveryBundleStatus.FAILED
            ProofOfDeliveryBundleStatus.DONE -> return next == ProofOfDeliveryBundleStatus.FAILED
        }
        return false
    }

    fun moveTo(next: ProofOfDeliveryBundleStatus) {
        if (!canMoveTo(next)) {
            error("proof of delivery bundle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun projectedExposure(): Double = round(capacity.toDouble() * distanceKm * 100) / 100

    fun snapshot(): ProofOfDeliveryBundle {
        return ProofOfDeliveryBundle(
            code = code,
            title = title,
            depot = depot,
            source = source,
            distanceKm = distanceKm,
            tags = tags,
            capacity = capacity,
            ratio = ratio,
            minimum = minimum,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a proof of delivery bundle in status new. */
fun newProofOfDeliveryBundle(code: String, title: String): ProofOfDeliveryBundle {
    return ProofOfDeliveryBundle(
        code = code,
        title = title,
        depot = "",
        source = "",
        distanceKm = 0.0,
        tags = mutableListOf<String>(),
        capacity = 0,
        ratio = 0.0,
        minimum = 0.0,
        category = "",
        status = ProofOfDeliveryBundleStatus.NEW,
    )
}
