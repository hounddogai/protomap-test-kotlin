package com.acme.transport.linehaul.laneplan.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lane plan manifest is in its life cycle. */
enum class LanePlanManifestStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a lane plan manifest and its current status. */
data class LanePlanManifest(
    var code: String,
    var title: String,
    var durationMinutes: Int,
    var dock: String,
    var expiresAt: Int,
    var attributes: MutableMap<String, String>,
    var etaMinutes: Int,
    var amount: Double,
    var status: LanePlanManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LanePlanManifestStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: LanePlanManifestStatus): Boolean {
        when (status) {
            LanePlanManifestStatus.PROPOSED -> return next == LanePlanManifestStatus.APPROVED || next == LanePlanManifestStatus.REJECTED
            LanePlanManifestStatus.APPROVED -> return next == LanePlanManifestStatus.REJECTED || next == LanePlanManifestStatus.WITHDRAWN
            LanePlanManifestStatus.REJECTED -> {
                when (next) {
                    LanePlanManifestStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LanePlanManifestStatus) {
        if (!canMoveTo(next)) {
            error("lane plan manifest $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedValue(): Double = round((expiresAt.toDouble() - amount) * 100) / 100

    fun snapshot(): LanePlanManifest {
        return LanePlanManifest(
            code = code,
            title = title,
            durationMinutes = durationMinutes,
            dock = dock,
            expiresAt = expiresAt,
            attributes = attributes,
            etaMinutes = etaMinutes,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a lane plan manifest in status proposed. */
fun newLanePlanManifest(code: String, title: String): LanePlanManifest {
    return LanePlanManifest(
        code = code,
        title = title,
        durationMinutes = 0,
        dock = "",
        expiresAt = 0,
        attributes = mutableMapOf<String, String>(),
        etaMinutes = 0,
        amount = 0.0,
        status = LanePlanManifestStatus.PROPOSED,
    )
}
