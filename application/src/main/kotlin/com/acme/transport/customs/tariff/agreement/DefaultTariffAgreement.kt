package com.acme.transport.customs.tariff.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The default tariff agreement records of customs's tariff work, and the rules, stores, and calculations
// around them.

/** Where a default tariff agreement is in its life cycle. */
enum class DefaultTariffAgreementStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One default tariff agreement, with what the planners need to act on it. */
data class DefaultTariffAgreement(
    var code: String,
    var name: String,
    var limit: Int,
    var visible: Boolean,
    var ownerId: String,
    var country: String,
    var vehicleId: String,
    var summary: String,
    var batchSize: Int,
    var status: DefaultTariffAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultTariffAgreementStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultTariffAgreementStatus): Boolean {
        when (status) {
            DefaultTariffAgreementStatus.DRAFT -> return next == DefaultTariffAgreementStatus.ACTIVE || next == DefaultTariffAgreementStatus.SUSPENDED
            DefaultTariffAgreementStatus.PENDING -> {
                when (next) {
                    DefaultTariffAgreementStatus.CLOSED -> return true
                }
                return false
            }
            DefaultTariffAgreementStatus.ACTIVE -> {
                when (next) {
                    DefaultTariffAgreementStatus.SUSPENDED, DefaultTariffAgreementStatus.CLOSED -> return true
                }
                return false
            }
            DefaultTariffAgreementStatus.SUSPENDED -> return next == DefaultTariffAgreementStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: DefaultTariffAgreementStatus) {
        if (!canMoveTo(next)) {
            error("default tariff agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"default tariff agreement"} $code"

    fun netVolume(): Double = round((limit.toDouble() - batchSize.toDouble()) * 100) / 100

    fun snapshot(): DefaultTariffAgreement {
        return DefaultTariffAgreement(
            code = code,
            name = name,
            limit = limit,
            visible = visible,
            ownerId = ownerId,
            country = country,
            vehicleId = vehicleId,
            summary = summary,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a default tariff agreement in status draft. */
fun newDefaultTariffAgreement(code: String, name: String): DefaultTariffAgreement {
    return DefaultTariffAgreement(
        code = code,
        name = name,
        limit = 0,
        visible = false,
        ownerId = "",
        country = "",
        vehicleId = "",
        summary = "",
        batchSize = 0,
        status = DefaultTariffAgreementStatus.DRAFT,
    )
}
