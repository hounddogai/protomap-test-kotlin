package com.acme.transport.lastmile.proofofdelivery.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The primary proof of delivery notice records of lastmile's proof of delivery work, and the rules, stores,
// and calculations around them.

/** Where a primary proof of delivery notice is in its life cycle. */
enum class PrimaryProofOfDeliveryNoticeStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A primary proof of delivery notice as the proof of delivery team keeps it. */
data class PrimaryProofOfDeliveryNotice(
    var code: String,
    var name: String,
    var discount: Double,
    var maximum: Double,
    var capacity: Int,
    var limit: Int,
    var ratio: Double,
    var attributes: MutableMap<String, String>,
    var priority: Int,
    var vehicleId: String,
    var status: PrimaryProofOfDeliveryNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryProofOfDeliveryNoticeStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryProofOfDeliveryNoticeStatus): Boolean {
        when (status) {
            PrimaryProofOfDeliveryNoticeStatus.REQUESTED -> return next == PrimaryProofOfDeliveryNoticeStatus.REVOKED || next == PrimaryProofOfDeliveryNoticeStatus.EXPIRED
            PrimaryProofOfDeliveryNoticeStatus.GRANTED -> return next == PrimaryProofOfDeliveryNoticeStatus.REVOKED || next == PrimaryProofOfDeliveryNoticeStatus.EXPIRED
            PrimaryProofOfDeliveryNoticeStatus.REVOKED -> return next == PrimaryProofOfDeliveryNoticeStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: PrimaryProofOfDeliveryNoticeStatus) {
        if (!canMoveTo(next)) {
            error("primary proof of delivery notice $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netVolume(): Double = round((capacity.toDouble() - ratio) * 100) / 100

    fun snapshot(): PrimaryProofOfDeliveryNotice {
        return PrimaryProofOfDeliveryNotice(
            code = code,
            name = name,
            discount = discount,
            maximum = maximum,
            capacity = capacity,
            limit = limit,
            ratio = ratio,
            attributes = attributes,
            priority = priority,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a primary proof of delivery notice in status requested. */
fun newPrimaryProofOfDeliveryNotice(code: String, name: String): PrimaryProofOfDeliveryNotice {
    return PrimaryProofOfDeliveryNotice(
        code = code,
        name = name,
        discount = 0.0,
        maximum = 0.0,
        capacity = 0,
        limit = 0,
        ratio = 0.0,
        attributes = mutableMapOf<String, String>(),
        priority = 0,
        vehicleId = "",
        status = PrimaryProofOfDeliveryNoticeStatus.REQUESTED,
    )
}
