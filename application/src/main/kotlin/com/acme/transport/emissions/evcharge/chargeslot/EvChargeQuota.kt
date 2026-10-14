package com.acme.transport.emissions.evcharge.chargeslot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a ev charge quota is in its life cycle. */
enum class EvChargeQuotaStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A ev charge quota. Its code is unique within a tenant. */
data class EvChargeQuota(
    var code: String,
    var title: String,
    var graceDays: Int,
    var etaMinutes: Int,
    var maximum: Double,
    var batchSize: Int,
    var limit: Int,
    var pinned: Boolean,
    var checksum: String,
    var attributes: MutableMap<String, String>,
    var status: EvChargeQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EvChargeQuotaStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: EvChargeQuotaStatus): Boolean {
        when (status) {
            EvChargeQuotaStatus.OPEN -> {
                when (next) {
                    EvChargeQuotaStatus.RESOLVED -> return true
                }
                return false
            }
            EvChargeQuotaStatus.IN_REVIEW -> return next == EvChargeQuotaStatus.REOPENED
            EvChargeQuotaStatus.RESOLVED -> return next == EvChargeQuotaStatus.REOPENED
            EvChargeQuotaStatus.REOPENED -> return next == EvChargeQuotaStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: EvChargeQuotaStatus) {
        if (!canMoveTo(next)) {
            error("ev charge quota $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"ev charge quota"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netVolume(): Double = round(etaMinutes.toDouble() * limit.toDouble() * 100) / 100

    fun snapshot(): EvChargeQuota {
        return EvChargeQuota(
            code = code,
            title = title,
            graceDays = graceDays,
            etaMinutes = etaMinutes,
            maximum = maximum,
            batchSize = batchSize,
            limit = limit,
            pinned = pinned,
            checksum = checksum,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a ev charge quota in status open. */
fun newEvChargeQuota(code: String, title: String): EvChargeQuota {
    return EvChargeQuota(
        code = code,
        title = title,
        graceDays = 0,
        etaMinutes = 0,
        maximum = 0.0,
        batchSize = 0,
        limit = 0,
        pinned = false,
        checksum = "",
        attributes = mutableMapOf<String, String>(),
        status = EvChargeQuotaStatus.OPEN,
    )
}
