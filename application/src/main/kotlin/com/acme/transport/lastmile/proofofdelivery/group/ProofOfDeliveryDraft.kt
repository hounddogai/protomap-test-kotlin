package com.acme.transport.lastmile.proofofdelivery.group

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a proof of delivery draft is in its life cycle. */
enum class ProofOfDeliveryDraftStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A proof of delivery draft. Its code is unique within a tenant. */
data class ProofOfDeliveryDraft(
    var code: String,
    var title: String,
    var fuelLiters: Double,
    var graceDays: Int,
    var routeCode: String,
    var budget: Double,
    var approved: Boolean,
    var description: String,
    var loadKg: Double,
    var status: ProofOfDeliveryDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ProofOfDeliveryDraftStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ProofOfDeliveryDraftStatus): Boolean {
        when (status) {
            ProofOfDeliveryDraftStatus.REQUESTED -> {
                when (next) {
                    ProofOfDeliveryDraftStatus.REVOKED -> return true
                }
                return false
            }
            ProofOfDeliveryDraftStatus.GRANTED -> {
                when (next) {
                    ProofOfDeliveryDraftStatus.REVOKED, ProofOfDeliveryDraftStatus.EXPIRED -> return true
                }
                return false
            }
            ProofOfDeliveryDraftStatus.REVOKED -> return next == ProofOfDeliveryDraftStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: ProofOfDeliveryDraftStatus) {
        if (!canMoveTo(next)) {
            error("proof of delivery draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun netVolume(): Double = round(graceDays.toDouble() * budget * 100) / 100

    fun snapshot(): ProofOfDeliveryDraft {
        return ProofOfDeliveryDraft(
            code = code,
            title = title,
            fuelLiters = fuelLiters,
            graceDays = graceDays,
            routeCode = routeCode,
            budget = budget,
            approved = approved,
            description = description,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a proof of delivery draft in status requested. */
fun newProofOfDeliveryDraft(code: String, title: String): ProofOfDeliveryDraft {
    return ProofOfDeliveryDraft(
        code = code,
        title = title,
        fuelLiters = 0.0,
        graceDays = 0,
        routeCode = "",
        budget = 0.0,
        approved = false,
        description = "",
        loadKg = 0.0,
        status = ProofOfDeliveryDraftStatus.REQUESTED,
    )
}
