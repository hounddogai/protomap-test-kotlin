package com.acme.transport.emissions.emissionfactor.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a emission factor reservation is in its life cycle. */
enum class EmissionFactorReservationStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One emission factor reservation, with what the planners need to act on it. */
data class EmissionFactorReservation(
    var code: String,
    var title: String,
    var margin: Double,
    var cost: Double,
    var sequence: Int,
    var region: String,
    var ownerId: String,
    var visible: Boolean,
    var active: Boolean,
    var phone: String,
    var priority: Int,
    var status: EmissionFactorReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EmissionFactorReservationStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: EmissionFactorReservationStatus): Boolean {
        when (status) {
            EmissionFactorReservationStatus.CREATED -> {
                when (next) {
                    EmissionFactorReservationStatus.RETIRED -> return true
                }
                return false
            }
            EmissionFactorReservationStatus.VALIDATED -> {
                when (next) {
                    EmissionFactorReservationStatus.PUBLISHED -> return true
                }
                return false
            }
            EmissionFactorReservationStatus.PUBLISHED -> {
                when (next) {
                    EmissionFactorReservationStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: EmissionFactorReservationStatus) {
        if (!canMoveTo(next)) {
            error("emission factor reservation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"emission factor reservation"} $code"

    fun effectiveLoad(): Double = round((sequence.toDouble() - margin) * 100) / 100

    fun snapshot(): EmissionFactorReservation {
        return EmissionFactorReservation(
            code = code,
            title = title,
            margin = margin,
            cost = cost,
            sequence = sequence,
            region = region,
            ownerId = ownerId,
            visible = visible,
            active = active,
            phone = phone,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a emission factor reservation in status created. */
fun newEmissionFactorReservation(code: String, title: String): EmissionFactorReservation {
    return EmissionFactorReservation(
        code = code,
        title = title,
        margin = 0.0,
        cost = 0.0,
        sequence = 0,
        region = "",
        ownerId = "",
        visible = false,
        active = false,
        phone = "",
        priority = 0,
        status = EmissionFactorReservationStatus.CREATED,
    )
}
