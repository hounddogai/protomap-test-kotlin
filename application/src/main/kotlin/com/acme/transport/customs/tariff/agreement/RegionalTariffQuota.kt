package com.acme.transport.customs.tariff.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional tariff quota is in its life cycle. */
enum class RegionalTariffQuotaStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A regional tariff quota. Its code is unique within a tenant. */
data class RegionalTariffQuota(
    var code: String,
    var label: String,
    var discount: Double,
    var active: Boolean,
    var attempts: Int,
    var channel: String,
    var region: String,
    var retries: Int,
    var notes: String,
    var status: RegionalTariffQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalTariffQuotaStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalTariffQuotaStatus): Boolean {
        when (status) {
            RegionalTariffQuotaStatus.DRAFT -> return next == RegionalTariffQuotaStatus.PENDING || next == RegionalTariffQuotaStatus.ACTIVE
            RegionalTariffQuotaStatus.PENDING -> return next == RegionalTariffQuotaStatus.CLOSED
            RegionalTariffQuotaStatus.ACTIVE -> {
                when (next) {
                    RegionalTariffQuotaStatus.CLOSED -> return true
                }
                return false
            }
            RegionalTariffQuotaStatus.SUSPENDED -> return next == RegionalTariffQuotaStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: RegionalTariffQuotaStatus) {
        if (!canMoveTo(next)) {
            error("regional tariff quota $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"regional tariff quota"} $code"

    fun weightedLoad(): Double = round((discount - attempts.toDouble()) * 100) / 100

    fun snapshot(): RegionalTariffQuota {
        return RegionalTariffQuota(
            code = code,
            label = label,
            discount = discount,
            active = active,
            attempts = attempts,
            channel = channel,
            region = region,
            retries = retries,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a regional tariff quota in status draft. */
fun newRegionalTariffQuota(code: String, label: String): RegionalTariffQuota {
    return RegionalTariffQuota(
        code = code,
        label = label,
        discount = 0.0,
        active = false,
        attempts = 0,
        channel = "",
        region = "",
        retries = 0,
        notes = "",
        status = RegionalTariffQuotaStatus.DRAFT,
    )
}
