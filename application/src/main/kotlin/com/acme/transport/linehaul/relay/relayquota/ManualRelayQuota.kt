package com.acme.transport.linehaul.relay.relayquota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The manual relay quota records of linehaul's relay work, and the rules, stores, and calculations around
// them.

/** Where a manual relay quota is in its life cycle. */
enum class ManualRelayQuotaStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A manual relay quota. Its code is unique within a tenant. */
data class ManualRelayQuota(
    var code: String,
    var title: String,
    var loadKg: Double,
    var stopCount: Int,
    var attributes: MutableMap<String, String>,
    var pinned: Boolean,
    var status: ManualRelayQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualRelayQuotaStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualRelayQuotaStatus): Boolean {
        when (status) {
            ManualRelayQuotaStatus.REQUESTED -> return next == ManualRelayQuotaStatus.REVOKED
            ManualRelayQuotaStatus.GRANTED -> {
                when (next) {
                    ManualRelayQuotaStatus.EXPIRED -> return true
                }
                return false
            }
            ManualRelayQuotaStatus.REVOKED -> {
                when (next) {
                    ManualRelayQuotaStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualRelayQuotaStatus) {
        if (!canMoveTo(next)) {
            error("manual relay quota $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"manual relay quota"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedVolume(): Double = round((stopCount.toDouble() + loadKg) * 100) / 100

    fun snapshot(): ManualRelayQuota {
        return ManualRelayQuota(
            code = code,
            title = title,
            loadKg = loadKg,
            stopCount = stopCount,
            attributes = attributes,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a manual relay quota in status requested. */
fun newManualRelayQuota(code: String, title: String): ManualRelayQuota {
    return ManualRelayQuota(
        code = code,
        title = title,
        loadKg = 0.0,
        stopCount = 0,
        attributes = mutableMapOf<String, String>(),
        pinned = false,
        status = ManualRelayQuotaStatus.REQUESTED,
    )
}
