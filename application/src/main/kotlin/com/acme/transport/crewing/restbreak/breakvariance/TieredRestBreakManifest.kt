package com.acme.transport.crewing.restbreak.breakvariance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered rest break manifest is in its life cycle. */
enum class TieredRestBreakManifestStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a tiered rest break manifest and its current status. */
data class TieredRestBreakManifest(
    var code: String,
    var name: String,
    var archived: Boolean,
    var threshold: Double,
    var odometer: Int,
    var status: TieredRestBreakManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredRestBreakManifestStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredRestBreakManifestStatus): Boolean {
        when (status) {
            TieredRestBreakManifestStatus.NEW -> {
                when (next) {
                    TieredRestBreakManifestStatus.DONE, TieredRestBreakManifestStatus.FAILED -> return true
                }
                return false
            }
            TieredRestBreakManifestStatus.QUEUED -> return next == TieredRestBreakManifestStatus.DONE
            TieredRestBreakManifestStatus.RUNNING -> return next == TieredRestBreakManifestStatus.DONE || next == TieredRestBreakManifestStatus.FAILED
            TieredRestBreakManifestStatus.DONE -> {
                when (next) {
                    TieredRestBreakManifestStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredRestBreakManifestStatus) {
        if (!canMoveTo(next)) {
            error("tiered rest break manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun effectiveExposure(): Double = round((threshold - odometer.toDouble()) * 100) / 100

    fun snapshot(): TieredRestBreakManifest {
        return TieredRestBreakManifest(
            code = code,
            name = name,
            archived = archived,
            threshold = threshold,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a tiered rest break manifest in status new. */
fun newTieredRestBreakManifest(code: String, name: String): TieredRestBreakManifest {
    return TieredRestBreakManifest(
        code = code,
        name = name,
        archived = false,
        threshold = 0.0,
        odometer = 0,
        status = TieredRestBreakManifestStatus.NEW,
    )
}
