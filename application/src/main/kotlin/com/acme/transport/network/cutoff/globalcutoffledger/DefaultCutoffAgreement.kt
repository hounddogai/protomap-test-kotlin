package com.acme.transport.network.cutoff.globalcutoffledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default cutoff agreement is in its life cycle. */
enum class DefaultCutoffAgreementStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A default cutoff agreement as the cutoff team keeps it. */
data class DefaultCutoffAgreement(
    var code: String,
    var title: String,
    var durationMinutes: Int,
    var createdAt: Int,
    var currency: String,
    var depot: String,
    var quantity: Int,
    var cost: Double,
    var distanceKm: Double,
    var shiftCode: String,
    var status: DefaultCutoffAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultCutoffAgreementStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultCutoffAgreementStatus): Boolean {
        when (status) {
            DefaultCutoffAgreementStatus.PLANNED -> {
                when (next) {
                    DefaultCutoffAgreementStatus.SCHEDULED, DefaultCutoffAgreementStatus.CANCELLED -> return true
                }
                return false
            }
            DefaultCutoffAgreementStatus.SCHEDULED -> return next == DefaultCutoffAgreementStatus.IN_PROGRESS || next == DefaultCutoffAgreementStatus.COMPLETED
            DefaultCutoffAgreementStatus.IN_PROGRESS -> return next == DefaultCutoffAgreementStatus.COMPLETED
            DefaultCutoffAgreementStatus.COMPLETED -> {
                when (next) {
                    DefaultCutoffAgreementStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultCutoffAgreementStatus) {
        if (!canMoveTo(next)) {
            error("default cutoff agreement $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun projectedLoad(): Double = round(quantity.toDouble() * distanceKm * 100) / 100

    fun snapshot(): DefaultCutoffAgreement {
        return DefaultCutoffAgreement(
            code = code,
            title = title,
            durationMinutes = durationMinutes,
            createdAt = createdAt,
            currency = currency,
            depot = depot,
            quantity = quantity,
            cost = cost,
            distanceKm = distanceKm,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a default cutoff agreement in status planned. */
fun newDefaultCutoffAgreement(code: String, title: String): DefaultCutoffAgreement {
    return DefaultCutoffAgreement(
        code = code,
        title = title,
        durationMinutes = 0,
        createdAt = 0,
        currency = "",
        depot = "",
        quantity = 0,
        cost = 0.0,
        distanceKm = 0.0,
        shiftCode = "",
        status = DefaultCutoffAgreementStatus.PLANNED,
    )
}
