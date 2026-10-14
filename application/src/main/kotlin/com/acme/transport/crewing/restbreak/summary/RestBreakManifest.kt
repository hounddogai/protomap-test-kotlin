package com.acme.transport.crewing.restbreak.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rest break manifest is in its life cycle. */
enum class RestBreakManifestStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A rest break manifest. Its code is unique within a tenant. */
data class RestBreakManifest(
    var code: String,
    var label: String,
    var createdAt: Int,
    var batchSize: Int,
    var summary: String,
    var notes: String,
    var fuelLiters: Double,
    var expiresAt: Int,
    var status: RestBreakManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RestBreakManifestStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RestBreakManifestStatus): Boolean {
        when (status) {
            RestBreakManifestStatus.PROPOSED -> {
                when (next) {
                    RestBreakManifestStatus.APPROVED, RestBreakManifestStatus.REJECTED -> return true
                }
                return false
            }
            RestBreakManifestStatus.APPROVED -> return next == RestBreakManifestStatus.REJECTED || next == RestBreakManifestStatus.WITHDRAWN
            RestBreakManifestStatus.REJECTED -> return next == RestBreakManifestStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: RestBreakManifestStatus) {
        if (!canMoveTo(next)) {
            error("rest break manifest $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun effectiveLoad(): Double = round((createdAt.toDouble() - expiresAt.toDouble()) * 100) / 100

    fun snapshot(): RestBreakManifest {
        return RestBreakManifest(
            code = code,
            label = label,
            createdAt = createdAt,
            batchSize = batchSize,
            summary = summary,
            notes = notes,
            fuelLiters = fuelLiters,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a rest break manifest in status proposed. */
fun newRestBreakManifest(code: String, label: String): RestBreakManifest {
    return RestBreakManifest(
        code = code,
        label = label,
        createdAt = 0,
        batchSize = 0,
        summary = "",
        notes = "",
        fuelLiters = 0.0,
        expiresAt = 0,
        status = RestBreakManifestStatus.PROPOSED,
    )
}
