package com.acme.transport.lastmile.locker.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped locker manifest is in its life cycle. */
enum class CappedLockerManifestStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One capped locker manifest, with what the planners need to act on it. */
data class CappedLockerManifest(
    var code: String,
    var name: String,
    var threshold: Double,
    var weight: Double,
    var loadKg: Double,
    var locale: String,
    var cost: Double,
    var status: CappedLockerManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedLockerManifestStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedLockerManifestStatus): Boolean {
        when (status) {
            CappedLockerManifestStatus.DRAFT -> {
                when (next) {
                    CappedLockerManifestStatus.ACTIVE -> return true
                }
                return false
            }
            CappedLockerManifestStatus.PENDING -> {
                when (next) {
                    CappedLockerManifestStatus.CLOSED -> return true
                }
                return false
            }
            CappedLockerManifestStatus.ACTIVE -> return next == CappedLockerManifestStatus.SUSPENDED || next == CappedLockerManifestStatus.CLOSED
            CappedLockerManifestStatus.SUSPENDED -> {
                when (next) {
                    CappedLockerManifestStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedLockerManifestStatus) {
        if (!canMoveTo(next)) {
            error("capped locker manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun weightedExposure(): Double = round((cost + loadKg) * 100) / 100

    fun snapshot(): CappedLockerManifest {
        return CappedLockerManifest(
            code = code,
            name = name,
            threshold = threshold,
            weight = weight,
            loadKg = loadKg,
            locale = locale,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a capped locker manifest in status draft. */
fun newCappedLockerManifest(code: String, name: String): CappedLockerManifest {
    return CappedLockerManifest(
        code = code,
        name = name,
        threshold = 0.0,
        weight = 0.0,
        loadKg = 0.0,
        locale = "",
        cost = 0.0,
        status = CappedLockerManifestStatus.DRAFT,
    )
}
