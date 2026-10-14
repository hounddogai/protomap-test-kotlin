package com.acme.transport.lastmile.parcel.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global parcel manifest is in its life cycle. */
enum class GlobalParcelManifestStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A global parcel manifest. Its code is unique within a tenant. */
data class GlobalParcelManifest(
    var code: String,
    var title: String,
    var description: String,
    var country: String,
    var expiresAt: Int,
    var driverId: String,
    var odometer: Int,
    var limit: Int,
    var discount: Double,
    var retries: Int,
    var status: GlobalParcelManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalParcelManifestStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalParcelManifestStatus): Boolean {
        when (status) {
            GlobalParcelManifestStatus.DRAFT -> return next == GlobalParcelManifestStatus.SUSPENDED || next == GlobalParcelManifestStatus.CLOSED
            GlobalParcelManifestStatus.PENDING -> return next == GlobalParcelManifestStatus.SUSPENDED || next == GlobalParcelManifestStatus.CLOSED
            GlobalParcelManifestStatus.ACTIVE -> return next == GlobalParcelManifestStatus.SUSPENDED || next == GlobalParcelManifestStatus.CLOSED
            GlobalParcelManifestStatus.SUSPENDED -> return next == GlobalParcelManifestStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: GlobalParcelManifestStatus) {
        if (!canMoveTo(next)) {
            error("global parcel manifest $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun effectiveLoad(): Double = round((retries.toDouble() - odometer.toDouble()) * 100) / 100

    fun snapshot(): GlobalParcelManifest {
        return GlobalParcelManifest(
            code = code,
            title = title,
            description = description,
            country = country,
            expiresAt = expiresAt,
            driverId = driverId,
            odometer = odometer,
            limit = limit,
            discount = discount,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a global parcel manifest in status draft. */
fun newGlobalParcelManifest(code: String, title: String): GlobalParcelManifest {
    return GlobalParcelManifest(
        code = code,
        title = title,
        description = "",
        country = "",
        expiresAt = 0,
        driverId = "",
        odometer = 0,
        limit = 0,
        discount = 0.0,
        retries = 0,
        status = GlobalParcelManifestStatus.DRAFT,
    )
}
