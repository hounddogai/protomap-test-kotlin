package com.acme.transport.customs.tariff.tariffdraft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tariff request is in its life cycle. */
enum class TariffRequestStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One tariff request, with what the planners need to act on it. */
data class TariffRequest(
    var code: String,
    var title: String,
    var discount: Double,
    var phone: String,
    var ceiling: Double,
    var visible: Boolean,
    var budget: Double,
    var pinned: Boolean,
    var status: TariffRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TariffRequestStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: TariffRequestStatus): Boolean {
        when (status) {
            TariffRequestStatus.DRAFT -> {
                when (next) {
                    TariffRequestStatus.ACTIVE, TariffRequestStatus.SUSPENDED -> return true
                }
                return false
            }
            TariffRequestStatus.PENDING -> {
                when (next) {
                    TariffRequestStatus.ACTIVE -> return true
                }
                return false
            }
            TariffRequestStatus.ACTIVE -> return next == TariffRequestStatus.SUSPENDED
            TariffRequestStatus.SUSPENDED -> return next == TariffRequestStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: TariffRequestStatus) {
        if (!canMoveTo(next)) {
            error("tariff request $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"tariff request"} $code"

    fun adjustedLoad(): Double = round((ceiling + budget) * 100) / 100

    fun snapshot(): TariffRequest {
        return TariffRequest(
            code = code,
            title = title,
            discount = discount,
            phone = phone,
            ceiling = ceiling,
            visible = visible,
            budget = budget,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a tariff request in status draft. */
fun newTariffRequest(code: String, title: String): TariffRequest {
    return TariffRequest(
        code = code,
        title = title,
        discount = 0.0,
        phone = "",
        ceiling = 0.0,
        visible = false,
        budget = 0.0,
        pinned = false,
        status = TariffRequestStatus.DRAFT,
    )
}
