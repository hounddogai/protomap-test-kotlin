package com.acme.transport.safety.nearmiss.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a near miss manifest is in its life cycle. */
enum class NearMissManifestStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a near miss manifest and its current status. */
data class NearMissManifest(
    var code: String,
    var name: String,
    var description: String,
    var windowDays: Int,
    var margin: Double,
    var maximum: Double,
    var status: NearMissManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NearMissManifestStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: NearMissManifestStatus): Boolean {
        when (status) {
            NearMissManifestStatus.NEW -> return next == NearMissManifestStatus.QUEUED || next == NearMissManifestStatus.RUNNING
            NearMissManifestStatus.QUEUED -> {
                when (next) {
                    NearMissManifestStatus.RUNNING, NearMissManifestStatus.FAILED -> return true
                }
                return false
            }
            NearMissManifestStatus.RUNNING -> return next == NearMissManifestStatus.DONE || next == NearMissManifestStatus.FAILED
            NearMissManifestStatus.DONE -> {
                when (next) {
                    NearMissManifestStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: NearMissManifestStatus) {
        if (!canMoveTo(next)) {
            error("near miss manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun weightedLoad(): Double = round((margin + windowDays.toDouble()) * 100) / 100

    fun snapshot(): NearMissManifest {
        return NearMissManifest(
            code = code,
            name = name,
            description = description,
            windowDays = windowDays,
            margin = margin,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a near miss manifest in status new. */
fun newNearMissManifest(code: String, name: String): NearMissManifest {
    return NearMissManifest(
        code = code,
        name = name,
        description = "",
        windowDays = 0,
        margin = 0.0,
        maximum = 0.0,
        status = NearMissManifestStatus.NEW,
    )
}
