package com.acme.transport.terminal.dwell.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The dwell digest records of terminal's dwell work, and the rules, stores, and calculations around them.

/** Where a dwell digest is in its life cycle. */
enum class DwellDigestStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A dwell digest. Its code is unique within a tenant. */
data class DwellDigest(
    var code: String,
    var label: String,
    var attempts: Int,
    var loadKg: Double,
    var dock: String,
    var expiresAt: Int,
    var status: DwellDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DwellDigestStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: DwellDigestStatus): Boolean {
        when (status) {
            DwellDigestStatus.PROPOSED -> return next == DwellDigestStatus.APPROVED || next == DwellDigestStatus.WITHDRAWN
            DwellDigestStatus.APPROVED -> return next == DwellDigestStatus.REJECTED
            DwellDigestStatus.REJECTED -> {
                when (next) {
                    DwellDigestStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DwellDigestStatus) {
        if (!canMoveTo(next)) {
            error("dwell digest $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun weightedTotal(): Double = round((expiresAt.toDouble() - loadKg) * 100) / 100

    fun snapshot(): DwellDigest {
        return DwellDigest(
            code = code,
            label = label,
            attempts = attempts,
            loadKg = loadKg,
            dock = dock,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a dwell digest in status proposed. */
fun newDwellDigest(code: String, label: String): DwellDigest {
    return DwellDigest(
        code = code,
        label = label,
        attempts = 0,
        loadKg = 0.0,
        dock = "",
        expiresAt = 0,
        status = DwellDigestStatus.PROPOSED,
    )
}
