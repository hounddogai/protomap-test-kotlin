package com.acme.transport.customs.tariff.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual tariff quota is in its life cycle. */
enum class ManualTariffQuotaStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a manual tariff quota and its current status. */
data class ManualTariffQuota(
    var code: String,
    var title: String,
    var locale: String,
    var reviewer: String,
    var batchSize: Int,
    var segment: String,
    var visible: Boolean,
    var active: Boolean,
    var margin: Double,
    var status: ManualTariffQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualTariffQuotaStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualTariffQuotaStatus): Boolean {
        when (status) {
            ManualTariffQuotaStatus.CREATED -> {
                when (next) {
                    ManualTariffQuotaStatus.VALIDATED, ManualTariffQuotaStatus.RETIRED -> return true
                }
                return false
            }
            ManualTariffQuotaStatus.VALIDATED -> return next == ManualTariffQuotaStatus.PUBLISHED
            ManualTariffQuotaStatus.PUBLISHED -> {
                when (next) {
                    ManualTariffQuotaStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualTariffQuotaStatus) {
        if (!canMoveTo(next)) {
            error("manual tariff quota $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun adjustedLoad(): Double = round((margin - batchSize.toDouble()) * 100) / 100

    fun snapshot(): ManualTariffQuota {
        return ManualTariffQuota(
            code = code,
            title = title,
            locale = locale,
            reviewer = reviewer,
            batchSize = batchSize,
            segment = segment,
            visible = visible,
            active = active,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a manual tariff quota in status created. */
fun newManualTariffQuota(code: String, title: String): ManualTariffQuota {
    return ManualTariffQuota(
        code = code,
        title = title,
        locale = "",
        reviewer = "",
        batchSize = 0,
        segment = "",
        visible = false,
        active = false,
        margin = 0.0,
        status = ManualTariffQuotaStatus.CREATED,
    )
}
