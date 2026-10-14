package com.acme.transport.lastmile.proofofdelivery.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a proof of delivery manifest is in its life cycle. */
enum class ProofOfDeliveryManifestStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One proof of delivery manifest, with what the planners need to act on it. */
data class ProofOfDeliveryManifest(
    var code: String,
    var label: String,
    var version: Int,
    var stopCount: Int,
    var limit: Int,
    var contactEmail: String,
    var active: Boolean,
    var approved: Boolean,
    var ratio: Double,
    var status: ProofOfDeliveryManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ProofOfDeliveryManifestStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ProofOfDeliveryManifestStatus): Boolean {
        when (status) {
            ProofOfDeliveryManifestStatus.CREATED -> {
                when (next) {
                    ProofOfDeliveryManifestStatus.VALIDATED -> return true
                }
                return false
            }
            ProofOfDeliveryManifestStatus.VALIDATED -> {
                when (next) {
                    ProofOfDeliveryManifestStatus.PUBLISHED, ProofOfDeliveryManifestStatus.RETIRED -> return true
                }
                return false
            }
            ProofOfDeliveryManifestStatus.PUBLISHED -> return next == ProofOfDeliveryManifestStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: ProofOfDeliveryManifestStatus) {
        if (!canMoveTo(next)) {
            error("proof of delivery manifest $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code [$label]"

    fun weightedValue(): Double = round((ratio - limit.toDouble()) * 100) / 100

    fun snapshot(): ProofOfDeliveryManifest {
        return ProofOfDeliveryManifest(
            code = code,
            label = label,
            version = version,
            stopCount = stopCount,
            limit = limit,
            contactEmail = contactEmail,
            active = active,
            approved = approved,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a proof of delivery manifest in status created. */
fun newProofOfDeliveryManifest(code: String, label: String): ProofOfDeliveryManifest {
    return ProofOfDeliveryManifest(
        code = code,
        label = label,
        version = 0,
        stopCount = 0,
        limit = 0,
        contactEmail = "",
        active = false,
        approved = false,
        ratio = 0.0,
        status = ProofOfDeliveryManifestStatus.CREATED,
    )
}
