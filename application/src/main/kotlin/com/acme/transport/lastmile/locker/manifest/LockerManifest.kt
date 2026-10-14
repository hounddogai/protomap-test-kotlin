package com.acme.transport.lastmile.locker.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The locker manifest records of lastmile's locker work, and the rules, stores, and calculations around them.

/** Where a locker manifest is in its life cycle. */
enum class LockerManifestStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A locker manifest. Its code is unique within a tenant. */
data class LockerManifest(
    var code: String,
    var name: String,
    var ceiling: Double,
    var amount: Double,
    var label: String,
    var startsAt: Int,
    var weight: Double,
    var threshold: Double,
    var minimum: Double,
    var limit: Int,
    var currency: String,
    var status: LockerManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LockerManifestStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LockerManifestStatus): Boolean {
        when (status) {
            LockerManifestStatus.IDLE -> {
                when (next) {
                    LockerManifestStatus.WARMING, LockerManifestStatus.STOPPED -> return true
                }
                return false
            }
            LockerManifestStatus.WARMING -> return next == LockerManifestStatus.DRAINING || next == LockerManifestStatus.STOPPED
            LockerManifestStatus.READY -> return next == LockerManifestStatus.DRAINING || next == LockerManifestStatus.STOPPED
            LockerManifestStatus.DRAINING -> return next == LockerManifestStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: LockerManifestStatus) {
        if (!canMoveTo(next)) {
            error("locker manifest $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $name"

    fun projectedTotal(): Double = round((minimum + startsAt.toDouble()) * 100) / 100

    fun snapshot(): LockerManifest {
        return LockerManifest(
            code = code,
            name = name,
            ceiling = ceiling,
            amount = amount,
            label = label,
            startsAt = startsAt,
            weight = weight,
            threshold = threshold,
            minimum = minimum,
            limit = limit,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a locker manifest in status idle. */
fun newLockerManifest(code: String, name: String): LockerManifest {
    return LockerManifest(
        code = code,
        name = name,
        ceiling = 0.0,
        amount = 0.0,
        label = "",
        startsAt = 0,
        weight = 0.0,
        threshold = 0.0,
        minimum = 0.0,
        limit = 0,
        currency = "",
        status = LockerManifestStatus.IDLE,
    )
}
