package com.acme.transport.customs.tariff.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered tariff bundle is in its life cycle. */
enum class TieredTariffBundleStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A tiered tariff bundle. Its code is unique within a tenant. */
data class TieredTariffBundle(
    var code: String,
    var name: String,
    var limit: Int,
    var country: String,
    var etaMinutes: Int,
    var status: TieredTariffBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredTariffBundleStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredTariffBundleStatus): Boolean {
        when (status) {
            TieredTariffBundleStatus.CREATED -> {
                when (next) {
                    TieredTariffBundleStatus.RETIRED -> return true
                }
                return false
            }
            TieredTariffBundleStatus.VALIDATED -> {
                when (next) {
                    TieredTariffBundleStatus.PUBLISHED, TieredTariffBundleStatus.RETIRED -> return true
                }
                return false
            }
            TieredTariffBundleStatus.PUBLISHED -> {
                when (next) {
                    TieredTariffBundleStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredTariffBundleStatus) {
        if (!canMoveTo(next)) {
            error("tiered tariff bundle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"tiered tariff bundle"} $code"

    fun effectiveValue(): Double = round(limit.toDouble() * etaMinutes.toDouble() * 100) / 100

    fun snapshot(): TieredTariffBundle {
        return TieredTariffBundle(
            code = code,
            name = name,
            limit = limit,
            country = country,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a tiered tariff bundle in status created. */
fun newTieredTariffBundle(code: String, name: String): TieredTariffBundle {
    return TieredTariffBundle(
        code = code,
        name = name,
        limit = 0,
        country = "",
        etaMinutes = 0,
        status = TieredTariffBundleStatus.CREATED,
    )
}
