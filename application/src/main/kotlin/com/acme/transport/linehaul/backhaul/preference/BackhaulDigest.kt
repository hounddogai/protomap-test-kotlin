package com.acme.transport.linehaul.backhaul.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a backhaul digest is in its life cycle. */
enum class BackhaulDigestStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a backhaul digest and its current status. */
data class BackhaulDigest(
    var code: String,
    var name: String,
    var cost: Double,
    var revision: Int,
    var loadKg: Double,
    var version: Int,
    var currency: String,
    var status: BackhaulDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BackhaulDigestStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: BackhaulDigestStatus): Boolean {
        when (status) {
            BackhaulDigestStatus.OPEN -> return next == BackhaulDigestStatus.ARCHIVED
            BackhaulDigestStatus.IN_REVIEW -> {
                when (next) {
                    BackhaulDigestStatus.RESOLVED, BackhaulDigestStatus.ARCHIVED -> return true
                }
                return false
            }
            BackhaulDigestStatus.RESOLVED -> return next == BackhaulDigestStatus.REOPENED || next == BackhaulDigestStatus.ARCHIVED
            BackhaulDigestStatus.REOPENED -> return next == BackhaulDigestStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: BackhaulDigestStatus) {
        if (!canMoveTo(next)) {
            error("backhaul digest $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"backhaul digest"} $code"

    fun netExposure(): Double = round(revision.toDouble() * loadKg * 100) / 100

    fun snapshot(): BackhaulDigest {
        return BackhaulDigest(
            code = code,
            name = name,
            cost = cost,
            revision = revision,
            loadKg = loadKg,
            version = version,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a backhaul digest in status open. */
fun newBackhaulDigest(code: String, name: String): BackhaulDigest {
    return BackhaulDigest(
        code = code,
        name = name,
        cost = 0.0,
        revision = 0,
        loadKg = 0.0,
        version = 0,
        currency = "",
        status = BackhaulDigestStatus.OPEN,
    )
}
