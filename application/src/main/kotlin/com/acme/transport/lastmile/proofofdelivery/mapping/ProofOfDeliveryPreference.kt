package com.acme.transport.lastmile.proofofdelivery.mapping

import com.acme.transport.kit.text.normalize

/** Where a proof of delivery preference is in its life cycle. */
enum class ProofOfDeliveryPreferenceStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A proof of delivery preference. Its code is unique within a tenant. */
data class ProofOfDeliveryPreference(
    var code: String,
    var label: String,
    var source: String,
    var minimum: Double,
    var routeCode: String,
    var status: ProofOfDeliveryPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ProofOfDeliveryPreferenceStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ProofOfDeliveryPreferenceStatus): Boolean {
        when (status) {
            ProofOfDeliveryPreferenceStatus.PLANNED -> {
                when (next) {
                    ProofOfDeliveryPreferenceStatus.IN_PROGRESS, ProofOfDeliveryPreferenceStatus.CANCELLED -> return true
                }
                return false
            }
            ProofOfDeliveryPreferenceStatus.SCHEDULED -> return next == ProofOfDeliveryPreferenceStatus.COMPLETED || next == ProofOfDeliveryPreferenceStatus.CANCELLED
            ProofOfDeliveryPreferenceStatus.IN_PROGRESS -> {
                when (next) {
                    ProofOfDeliveryPreferenceStatus.COMPLETED -> return true
                }
                return false
            }
            ProofOfDeliveryPreferenceStatus.COMPLETED -> {
                when (next) {
                    ProofOfDeliveryPreferenceStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ProofOfDeliveryPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("proof of delivery preference $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"proof of delivery preference"} $code"

    fun measuredValue(): Double = minimum

    fun snapshot(): ProofOfDeliveryPreference {
        return ProofOfDeliveryPreference(
            code = code,
            label = label,
            source = source,
            minimum = minimum,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a proof of delivery preference in status planned. */
fun newProofOfDeliveryPreference(code: String, label: String): ProofOfDeliveryPreference {
    return ProofOfDeliveryPreference(
        code = code,
        label = label,
        source = "",
        minimum = 0.0,
        routeCode = "",
        status = ProofOfDeliveryPreferenceStatus.PLANNED,
    )
}
