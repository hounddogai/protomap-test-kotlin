package com.acme.transport.lastmile.proofofdelivery.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The proof of delivery mapping records of lastmile's proof of delivery work, and the rules, stores, and
// calculations around them.

/** Where a proof of delivery mapping is in its life cycle. */
enum class ProofOfDeliveryMappingStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A proof of delivery mapping. Its code is unique within a tenant. */
data class ProofOfDeliveryMapping(
    var code: String,
    var name: String,
    var locale: String,
    var currency: String,
    var updatedAt: Int,
    var attributes: MutableMap<String, String>,
    var amount: Double,
    var budget: Double,
    var status: ProofOfDeliveryMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ProofOfDeliveryMappingStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ProofOfDeliveryMappingStatus): Boolean {
        when (status) {
            ProofOfDeliveryMappingStatus.CREATED -> return next == ProofOfDeliveryMappingStatus.VALIDATED || next == ProofOfDeliveryMappingStatus.RETIRED
            ProofOfDeliveryMappingStatus.VALIDATED -> {
                when (next) {
                    ProofOfDeliveryMappingStatus.PUBLISHED, ProofOfDeliveryMappingStatus.RETIRED -> return true
                }
                return false
            }
            ProofOfDeliveryMappingStatus.PUBLISHED -> return next == ProofOfDeliveryMappingStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: ProofOfDeliveryMappingStatus) {
        if (!canMoveTo(next)) {
            error("proof of delivery mapping $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netTotal(): Double = round((amount + updatedAt.toDouble()) * 100) / 100

    fun snapshot(): ProofOfDeliveryMapping {
        return ProofOfDeliveryMapping(
            code = code,
            name = name,
            locale = locale,
            currency = currency,
            updatedAt = updatedAt,
            attributes = attributes,
            amount = amount,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a proof of delivery mapping in status created. */
fun newProofOfDeliveryMapping(code: String, name: String): ProofOfDeliveryMapping {
    return ProofOfDeliveryMapping(
        code = code,
        name = name,
        locale = "",
        currency = "",
        updatedAt = 0,
        attributes = mutableMapOf<String, String>(),
        amount = 0.0,
        budget = 0.0,
        status = ProofOfDeliveryMappingStatus.CREATED,
    )
}
