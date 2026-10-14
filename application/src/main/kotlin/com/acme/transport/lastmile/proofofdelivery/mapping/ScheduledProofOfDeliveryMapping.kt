package com.acme.transport.lastmile.proofofdelivery.mapping

import com.acme.transport.kit.text.normalize

/** Where a scheduled proof of delivery mapping is in its life cycle. */
enum class ScheduledProofOfDeliveryMappingStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a scheduled proof of delivery mapping and its current status. */
data class ScheduledProofOfDeliveryMapping(
    var code: String,
    var name: String,
    var country: String,
    var sequence: Int,
    var notes: String,
    var status: ScheduledProofOfDeliveryMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledProofOfDeliveryMappingStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledProofOfDeliveryMappingStatus): Boolean {
        when (status) {
            ScheduledProofOfDeliveryMappingStatus.REQUESTED -> return next == ScheduledProofOfDeliveryMappingStatus.GRANTED || next == ScheduledProofOfDeliveryMappingStatus.EXPIRED
            ScheduledProofOfDeliveryMappingStatus.GRANTED -> return next == ScheduledProofOfDeliveryMappingStatus.REVOKED
            ScheduledProofOfDeliveryMappingStatus.REVOKED -> {
                when (next) {
                    ScheduledProofOfDeliveryMappingStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledProofOfDeliveryMappingStatus) {
        if (!canMoveTo(next)) {
            error("scheduled proof of delivery mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun measuredValue(): Double = sequence.toDouble()

    fun snapshot(): ScheduledProofOfDeliveryMapping {
        return ScheduledProofOfDeliveryMapping(
            code = code,
            name = name,
            country = country,
            sequence = sequence,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a scheduled proof of delivery mapping in status requested. */
fun newScheduledProofOfDeliveryMapping(code: String, name: String): ScheduledProofOfDeliveryMapping {
    return ScheduledProofOfDeliveryMapping(
        code = code,
        name = name,
        country = "",
        sequence = 0,
        notes = "",
        status = ScheduledProofOfDeliveryMappingStatus.REQUESTED,
    )
}
