package com.acme.transport.freightpay.ratecard.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default rate card audit is in its life cycle. */
enum class DefaultRateCardAuditStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A default rate card audit. Its code is unique within a tenant. */
data class DefaultRateCardAudit(
    var code: String,
    var name: String,
    var updatedAt: Int,
    var windowDays: Int,
    var dock: String,
    var approved: Boolean,
    var quantity: Int,
    var routeCode: String,
    var country: String,
    var category: String,
    var status: DefaultRateCardAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultRateCardAuditStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultRateCardAuditStatus): Boolean {
        when (status) {
            DefaultRateCardAuditStatus.DRAFT -> return next == DefaultRateCardAuditStatus.PENDING || next == DefaultRateCardAuditStatus.CLOSED
            DefaultRateCardAuditStatus.PENDING -> return next == DefaultRateCardAuditStatus.SUSPENDED || next == DefaultRateCardAuditStatus.CLOSED
            DefaultRateCardAuditStatus.ACTIVE -> return next == DefaultRateCardAuditStatus.SUSPENDED || next == DefaultRateCardAuditStatus.CLOSED
            DefaultRateCardAuditStatus.SUSPENDED -> {
                when (next) {
                    DefaultRateCardAuditStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultRateCardAuditStatus) {
        if (!canMoveTo(next)) {
            error("default rate card audit $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"default rate card audit"} $code"

    fun projectedVolume(): Double = round((quantity.toDouble() + updatedAt.toDouble()) * 100) / 100

    fun snapshot(): DefaultRateCardAudit {
        return DefaultRateCardAudit(
            code = code,
            name = name,
            updatedAt = updatedAt,
            windowDays = windowDays,
            dock = dock,
            approved = approved,
            quantity = quantity,
            routeCode = routeCode,
            country = country,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a default rate card audit in status draft. */
fun newDefaultRateCardAudit(code: String, name: String): DefaultRateCardAudit {
    return DefaultRateCardAudit(
        code = code,
        name = name,
        updatedAt = 0,
        windowDays = 0,
        dock = "",
        approved = false,
        quantity = 0,
        routeCode = "",
        country = "",
        category = "",
        status = DefaultRateCardAuditStatus.DRAFT,
    )
}
