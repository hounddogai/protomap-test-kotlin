package com.acme.transport.lastmile.proofofdelivery.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The weighted proof of delivery preference records of lastmile's proof of delivery work, and the rules,
// stores, and calculations around them.

/** Where a weighted proof of delivery preference is in its life cycle. */
enum class WeightedProofOfDeliveryPreferenceStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a weighted proof of delivery preference and its current status. */
data class WeightedProofOfDeliveryPreference(
    var code: String,
    var title: String,
    var phone: String,
    var createdAt: Int,
    var source: String,
    var margin: Double,
    var depot: String,
    var active: Boolean,
    var status: WeightedProofOfDeliveryPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedProofOfDeliveryPreferenceStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedProofOfDeliveryPreferenceStatus): Boolean {
        when (status) {
            WeightedProofOfDeliveryPreferenceStatus.CREATED -> {
                when (next) {
                    WeightedProofOfDeliveryPreferenceStatus.VALIDATED, WeightedProofOfDeliveryPreferenceStatus.RETIRED -> return true
                }
                return false
            }
            WeightedProofOfDeliveryPreferenceStatus.VALIDATED -> {
                when (next) {
                    WeightedProofOfDeliveryPreferenceStatus.RETIRED -> return true
                }
                return false
            }
            WeightedProofOfDeliveryPreferenceStatus.PUBLISHED -> {
                when (next) {
                    WeightedProofOfDeliveryPreferenceStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedProofOfDeliveryPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("weighted proof of delivery preference $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun projectedExposure(): Double = round(margin * createdAt.toDouble() * 100) / 100

    fun snapshot(): WeightedProofOfDeliveryPreference {
        return WeightedProofOfDeliveryPreference(
            code = code,
            title = title,
            phone = phone,
            createdAt = createdAt,
            source = source,
            margin = margin,
            depot = depot,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a weighted proof of delivery preference in status created. */
fun newWeightedProofOfDeliveryPreference(code: String, title: String): WeightedProofOfDeliveryPreference {
    return WeightedProofOfDeliveryPreference(
        code = code,
        title = title,
        phone = "",
        createdAt = 0,
        source = "",
        margin = 0.0,
        depot = "",
        active = false,
        status = WeightedProofOfDeliveryPreferenceStatus.CREATED,
    )
}
