package com.acme.transport.lastmile.locker.lockerclaim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a locker bundle is in its life cycle. */
enum class LockerBundleStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a locker bundle and its current status. */
data class LockerBundle(
    var code: String,
    var name: String,
    var dock: String,
    var expiresAt: Int,
    var label: String,
    var etaMinutes: Int,
    var shiftCode: String,
    var createdAt: Int,
    var ratio: Double,
    var externalRef: String,
    var status: LockerBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LockerBundleStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LockerBundleStatus): Boolean {
        when (status) {
            LockerBundleStatus.REQUESTED -> {
                when (next) {
                    LockerBundleStatus.EXPIRED -> return true
                }
                return false
            }
            LockerBundleStatus.GRANTED -> {
                when (next) {
                    LockerBundleStatus.REVOKED -> return true
                }
                return false
            }
            LockerBundleStatus.REVOKED -> return next == LockerBundleStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: LockerBundleStatus) {
        if (!canMoveTo(next)) {
            error("locker bundle $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"locker bundle"} $code"

    fun effectiveLoad(): Double = round(createdAt.toDouble() * ratio * 100) / 100

    fun snapshot(): LockerBundle {
        return LockerBundle(
            code = code,
            name = name,
            dock = dock,
            expiresAt = expiresAt,
            label = label,
            etaMinutes = etaMinutes,
            shiftCode = shiftCode,
            createdAt = createdAt,
            ratio = ratio,
            externalRef = externalRef,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a locker bundle in status requested. */
fun newLockerBundle(code: String, name: String): LockerBundle {
    return LockerBundle(
        code = code,
        name = name,
        dock = "",
        expiresAt = 0,
        label = "",
        etaMinutes = 0,
        shiftCode = "",
        createdAt = 0,
        ratio = 0.0,
        externalRef = "",
        status = LockerBundleStatus.REQUESTED,
    )
}
