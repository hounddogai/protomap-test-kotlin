package com.acme.transport.customs.tariff.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy tariff quota is in its life cycle. */
enum class LegacyTariffQuotaStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a legacy tariff quota and its current status. */
data class LegacyTariffQuota(
    var code: String,
    var title: String,
    var threshold: Double,
    var maximum: Double,
    var capacity: Int,
    var summary: String,
    var channel: String,
    var attributes: MutableMap<String, String>,
    var tags: MutableList<String>,
    var rate: Double,
    var status: LegacyTariffQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyTariffQuotaStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyTariffQuotaStatus): Boolean {
        when (status) {
            LegacyTariffQuotaStatus.NEW -> return next == LegacyTariffQuotaStatus.DONE || next == LegacyTariffQuotaStatus.FAILED
            LegacyTariffQuotaStatus.QUEUED -> return next == LegacyTariffQuotaStatus.DONE || next == LegacyTariffQuotaStatus.FAILED
            LegacyTariffQuotaStatus.RUNNING -> {
                when (next) {
                    LegacyTariffQuotaStatus.DONE, LegacyTariffQuotaStatus.FAILED -> return true
                }
                return false
            }
            LegacyTariffQuotaStatus.DONE -> {
                when (next) {
                    LegacyTariffQuotaStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyTariffQuotaStatus) {
        if (!canMoveTo(next)) {
            error("legacy tariff quota $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedTotal(): Double = round(rate * threshold * 100) / 100

    fun snapshot(): LegacyTariffQuota {
        return LegacyTariffQuota(
            code = code,
            title = title,
            threshold = threshold,
            maximum = maximum,
            capacity = capacity,
            summary = summary,
            channel = channel,
            attributes = attributes,
            tags = tags,
            rate = rate,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a legacy tariff quota in status new. */
fun newLegacyTariffQuota(code: String, title: String): LegacyTariffQuota {
    return LegacyTariffQuota(
        code = code,
        title = title,
        threshold = 0.0,
        maximum = 0.0,
        capacity = 0,
        summary = "",
        channel = "",
        attributes = mutableMapOf<String, String>(),
        tags = mutableListOf<String>(),
        rate = 0.0,
        status = LegacyTariffQuotaStatus.NEW,
    )
}
