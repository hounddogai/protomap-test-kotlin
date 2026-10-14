package com.acme.transport.safety.incident.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a incident agreement is in its life cycle. */
enum class IncidentAgreementStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A incident agreement. Its code is unique within a tenant. */
data class IncidentAgreement(
    var code: String,
    var name: String,
    var amount: Double,
    var description: String,
    var notes: String,
    var discount: Double,
    var capacity: Int,
    var startsAt: Int,
    var ratio: Double,
    var sequence: Int,
    var status: IncidentAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IncidentAgreementStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: IncidentAgreementStatus): Boolean {
        when (status) {
            IncidentAgreementStatus.IDLE -> {
                when (next) {
                    IncidentAgreementStatus.DRAINING -> return true
                }
                return false
            }
            IncidentAgreementStatus.WARMING -> return next == IncidentAgreementStatus.STOPPED
            IncidentAgreementStatus.READY -> return next == IncidentAgreementStatus.DRAINING || next == IncidentAgreementStatus.STOPPED
            IncidentAgreementStatus.DRAINING -> return next == IncidentAgreementStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: IncidentAgreementStatus) {
        if (!canMoveTo(next)) {
            error("incident agreement $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun adjustedValue(): Double = round((discount - ratio) * 100) / 100

    fun snapshot(): IncidentAgreement {
        return IncidentAgreement(
            code = code,
            name = name,
            amount = amount,
            description = description,
            notes = notes,
            discount = discount,
            capacity = capacity,
            startsAt = startsAt,
            ratio = ratio,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a incident agreement in status idle. */
fun newIncidentAgreement(code: String, name: String): IncidentAgreement {
    return IncidentAgreement(
        code = code,
        name = name,
        amount = 0.0,
        description = "",
        notes = "",
        discount = 0.0,
        capacity = 0,
        startsAt = 0,
        ratio = 0.0,
        sequence = 0,
        status = IncidentAgreementStatus.IDLE,
    )
}
