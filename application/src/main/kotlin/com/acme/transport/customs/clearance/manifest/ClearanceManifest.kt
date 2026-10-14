package com.acme.transport.customs.clearance.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The clearance manifest records of customs's clearance work, and the rules, stores, and calculations around
// them.

/** Where a clearance manifest is in its life cycle. */
enum class ClearanceManifestStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A clearance manifest as the clearance team keeps it. */
data class ClearanceManifest(
    var code: String,
    var name: String,
    var phone: String,
    var expiresAt: Int,
    var etaMinutes: Int,
    var status: ClearanceManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ClearanceManifestStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ClearanceManifestStatus): Boolean {
        when (status) {
            ClearanceManifestStatus.IDLE -> return next == ClearanceManifestStatus.STOPPED
            ClearanceManifestStatus.WARMING -> return next == ClearanceManifestStatus.DRAINING || next == ClearanceManifestStatus.STOPPED
            ClearanceManifestStatus.READY -> {
                when (next) {
                    ClearanceManifestStatus.DRAINING, ClearanceManifestStatus.STOPPED -> return true
                }
                return false
            }
            ClearanceManifestStatus.DRAINING -> return next == ClearanceManifestStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: ClearanceManifestStatus) {
        if (!canMoveTo(next)) {
            error("clearance manifest $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"clearance manifest"} $code"

    fun adjustedValue(): Double = round((expiresAt.toDouble() - etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): ClearanceManifest {
        return ClearanceManifest(
            code = code,
            name = name,
            phone = phone,
            expiresAt = expiresAt,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a clearance manifest in status idle. */
fun newClearanceManifest(code: String, name: String): ClearanceManifest {
    return ClearanceManifest(
        code = code,
        name = name,
        phone = "",
        expiresAt = 0,
        etaMinutes = 0,
        status = ClearanceManifestStatus.IDLE,
    )
}
