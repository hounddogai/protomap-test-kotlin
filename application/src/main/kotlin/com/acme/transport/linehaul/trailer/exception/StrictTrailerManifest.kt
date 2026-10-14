package com.acme.transport.linehaul.trailer.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict trailer manifest is in its life cycle. */
enum class StrictTrailerManifestStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A strict trailer manifest. Its code is unique within a tenant. */
data class StrictTrailerManifest(
    var code: String,
    var title: String,
    var batchSize: Int,
    var cost: Double,
    var threshold: Double,
    var country: String,
    var status: StrictTrailerManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictTrailerManifestStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictTrailerManifestStatus): Boolean {
        when (status) {
            StrictTrailerManifestStatus.NEW -> {
                when (next) {
                    StrictTrailerManifestStatus.QUEUED, StrictTrailerManifestStatus.DONE -> return true
                }
                return false
            }
            StrictTrailerManifestStatus.QUEUED -> {
                when (next) {
                    StrictTrailerManifestStatus.DONE, StrictTrailerManifestStatus.FAILED -> return true
                }
                return false
            }
            StrictTrailerManifestStatus.RUNNING -> return next == StrictTrailerManifestStatus.DONE
            StrictTrailerManifestStatus.DONE -> return next == StrictTrailerManifestStatus.FAILED
        }
        return false
    }

    fun moveTo(next: StrictTrailerManifestStatus) {
        if (!canMoveTo(next)) {
            error("strict trailer manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"strict trailer manifest"} $code"

    fun weightedTotal(): Double = round((batchSize.toDouble() - threshold) * 100) / 100

    fun snapshot(): StrictTrailerManifest {
        return StrictTrailerManifest(
            code = code,
            title = title,
            batchSize = batchSize,
            cost = cost,
            threshold = threshold,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a strict trailer manifest in status new. */
fun newStrictTrailerManifest(code: String, title: String): StrictTrailerManifest {
    return StrictTrailerManifest(
        code = code,
        title = title,
        batchSize = 0,
        cost = 0.0,
        threshold = 0.0,
        country = "",
        status = StrictTrailerManifestStatus.NEW,
    )
}
