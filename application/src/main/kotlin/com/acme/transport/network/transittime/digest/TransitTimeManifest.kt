package com.acme.transport.network.transittime.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a transit time manifest is in its life cycle. */
enum class TransitTimeManifestStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A transit time manifest as the transit time team keeps it. */
data class TransitTimeManifest(
    var code: String,
    var title: String,
    var maximum: Double,
    var tags: MutableList<String>,
    var notes: String,
    var minimum: Double,
    var status: TransitTimeManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TransitTimeManifestStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: TransitTimeManifestStatus): Boolean {
        when (status) {
            TransitTimeManifestStatus.NEW -> {
                when (next) {
                    TransitTimeManifestStatus.FAILED -> return true
                }
                return false
            }
            TransitTimeManifestStatus.QUEUED -> return next == TransitTimeManifestStatus.RUNNING
            TransitTimeManifestStatus.RUNNING -> return next == TransitTimeManifestStatus.DONE || next == TransitTimeManifestStatus.FAILED
            TransitTimeManifestStatus.DONE -> return next == TransitTimeManifestStatus.FAILED
        }
        return false
    }

    fun moveTo(next: TransitTimeManifestStatus) {
        if (!canMoveTo(next)) {
            error("transit time manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netTotal(): Double = round((maximum + minimum) * 100) / 100

    fun snapshot(): TransitTimeManifest {
        return TransitTimeManifest(
            code = code,
            title = title,
            maximum = maximum,
            tags = tags,
            notes = notes,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a transit time manifest in status new. */
fun newTransitTimeManifest(code: String, title: String): TransitTimeManifest {
    return TransitTimeManifest(
        code = code,
        title = title,
        maximum = 0.0,
        tags = mutableListOf<String>(),
        notes = "",
        minimum = 0.0,
        status = TransitTimeManifestStatus.NEW,
    )
}
