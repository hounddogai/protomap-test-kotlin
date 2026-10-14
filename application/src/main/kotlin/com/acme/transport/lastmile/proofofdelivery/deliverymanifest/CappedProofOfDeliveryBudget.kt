package com.acme.transport.lastmile.proofofdelivery.deliverymanifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped proof of delivery budget is in its life cycle. */
enum class CappedProofOfDeliveryBudgetStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A capped proof of delivery budget. Its code is unique within a tenant. */
data class CappedProofOfDeliveryBudget(
    var code: String,
    var label: String,
    var etaMinutes: Int,
    var carrier: String,
    var pinned: Boolean,
    var createdAt: Int,
    var notes: String,
    var attributes: MutableMap<String, String>,
    var expiresAt: Int,
    var driverId: String,
    var status: CappedProofOfDeliveryBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedProofOfDeliveryBudgetStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedProofOfDeliveryBudgetStatus): Boolean {
        when (status) {
            CappedProofOfDeliveryBudgetStatus.CREATED -> return next == CappedProofOfDeliveryBudgetStatus.VALIDATED || next == CappedProofOfDeliveryBudgetStatus.PUBLISHED
            CappedProofOfDeliveryBudgetStatus.VALIDATED -> {
                when (next) {
                    CappedProofOfDeliveryBudgetStatus.PUBLISHED, CappedProofOfDeliveryBudgetStatus.RETIRED -> return true
                }
                return false
            }
            CappedProofOfDeliveryBudgetStatus.PUBLISHED -> {
                when (next) {
                    CappedProofOfDeliveryBudgetStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedProofOfDeliveryBudgetStatus) {
        if (!canMoveTo(next)) {
            error("capped proof of delivery budget $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedTotal(): Double = round((expiresAt.toDouble() - etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): CappedProofOfDeliveryBudget {
        return CappedProofOfDeliveryBudget(
            code = code,
            label = label,
            etaMinutes = etaMinutes,
            carrier = carrier,
            pinned = pinned,
            createdAt = createdAt,
            notes = notes,
            attributes = attributes,
            expiresAt = expiresAt,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a capped proof of delivery budget in status created. */
fun newCappedProofOfDeliveryBudget(code: String, label: String): CappedProofOfDeliveryBudget {
    return CappedProofOfDeliveryBudget(
        code = code,
        label = label,
        etaMinutes = 0,
        carrier = "",
        pinned = false,
        createdAt = 0,
        notes = "",
        attributes = mutableMapOf<String, String>(),
        expiresAt = 0,
        driverId = "",
        status = CappedProofOfDeliveryBudgetStatus.CREATED,
    )
}
