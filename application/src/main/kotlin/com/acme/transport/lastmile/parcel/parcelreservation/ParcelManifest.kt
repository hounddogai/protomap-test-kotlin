package com.acme.transport.lastmile.parcel.parcelreservation

import com.acme.transport.kit.text.normalize

/** Where a parcel manifest is in its life cycle. */
enum class ParcelManifestStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A parcel manifest. Its code is unique within a tenant. */
data class ParcelManifest(
    var code: String,
    var label: String,
    var tags: MutableList<String>,
    var driverId: String,
    var externalRef: String,
    var retries: Int,
    var status: ParcelManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ParcelManifestStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ParcelManifestStatus): Boolean {
        when (status) {
            ParcelManifestStatus.IDLE -> {
                when (next) {
                    ParcelManifestStatus.WARMING, ParcelManifestStatus.STOPPED -> return true
                }
                return false
            }
            ParcelManifestStatus.WARMING -> return next == ParcelManifestStatus.READY
            ParcelManifestStatus.READY -> {
                when (next) {
                    ParcelManifestStatus.DRAINING -> return true
                }
                return false
            }
            ParcelManifestStatus.DRAINING -> return next == ParcelManifestStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: ParcelManifestStatus) {
        if (!canMoveTo(next)) {
            error("parcel manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"parcel manifest"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = retries.toDouble()

    fun snapshot(): ParcelManifest {
        return ParcelManifest(
            code = code,
            label = label,
            tags = tags,
            driverId = driverId,
            externalRef = externalRef,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a parcel manifest in status idle. */
fun newParcelManifest(code: String, label: String): ParcelManifest {
    return ParcelManifest(
        code = code,
        label = label,
        tags = mutableListOf<String>(),
        driverId = "",
        externalRef = "",
        retries = 0,
        status = ParcelManifestStatus.IDLE,
    )
}
