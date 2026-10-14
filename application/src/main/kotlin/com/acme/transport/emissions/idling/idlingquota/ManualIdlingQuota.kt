package com.acme.transport.emissions.idling.idlingquota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The manual idling quota records of emissions's idling work, and the rules, stores, and calculations around
// them.

/** Where a manual idling quota is in its life cycle. */
enum class ManualIdlingQuotaStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One manual idling quota, with what the planners need to act on it. */
data class ManualIdlingQuota(
    var code: String,
    var name: String,
    var pallets: Int,
    var updatedAt: Int,
    var phone: String,
    var revision: Int,
    var shiftCode: String,
    var retries: Int,
    var status: ManualIdlingQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualIdlingQuotaStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualIdlingQuotaStatus): Boolean {
        when (status) {
            ManualIdlingQuotaStatus.REQUESTED -> {
                when (next) {
                    ManualIdlingQuotaStatus.GRANTED, ManualIdlingQuotaStatus.REVOKED -> return true
                }
                return false
            }
            ManualIdlingQuotaStatus.GRANTED -> return next == ManualIdlingQuotaStatus.REVOKED || next == ManualIdlingQuotaStatus.EXPIRED
            ManualIdlingQuotaStatus.REVOKED -> return next == ManualIdlingQuotaStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: ManualIdlingQuotaStatus) {
        if (!canMoveTo(next)) {
            error("manual idling quota $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "${"manual idling quota"} $code"

    fun projectedExposure(): Double = round((revision.toDouble() - retries.toDouble()) * 100) / 100

    fun snapshot(): ManualIdlingQuota {
        return ManualIdlingQuota(
            code = code,
            name = name,
            pallets = pallets,
            updatedAt = updatedAt,
            phone = phone,
            revision = revision,
            shiftCode = shiftCode,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a manual idling quota in status requested. */
fun newManualIdlingQuota(code: String, name: String): ManualIdlingQuota {
    return ManualIdlingQuota(
        code = code,
        name = name,
        pallets = 0,
        updatedAt = 0,
        phone = "",
        revision = 0,
        shiftCode = "",
        retries = 0,
        status = ManualIdlingQuotaStatus.REQUESTED,
    )
}
