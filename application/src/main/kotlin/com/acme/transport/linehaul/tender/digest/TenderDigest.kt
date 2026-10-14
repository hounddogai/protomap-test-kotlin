package com.acme.transport.linehaul.tender.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tender digest records of linehaul's tender work, and the rules, stores, and calculations around them.

/** Where a tender digest is in its life cycle. */
enum class TenderDigestStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a tender digest and its current status. */
data class TenderDigest(
    var code: String,
    var title: String,
    var score: Double,
    var pallets: Int,
    var visible: Boolean,
    var routeCode: String,
    var expiresAt: Int,
    var threshold: Double,
    var status: TenderDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TenderDigestStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: TenderDigestStatus): Boolean {
        when (status) {
            TenderDigestStatus.OPEN -> {
                when (next) {
                    TenderDigestStatus.IN_REVIEW, TenderDigestStatus.ARCHIVED -> return true
                }
                return false
            }
            TenderDigestStatus.IN_REVIEW -> return next == TenderDigestStatus.RESOLVED
            TenderDigestStatus.RESOLVED -> {
                when (next) {
                    TenderDigestStatus.REOPENED, TenderDigestStatus.ARCHIVED -> return true
                }
                return false
            }
            TenderDigestStatus.REOPENED -> {
                when (next) {
                    TenderDigestStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TenderDigestStatus) {
        if (!canMoveTo(next)) {
            error("tender digest $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun adjustedTotal(): Double = round((score - expiresAt.toDouble()) * 100) / 100

    fun snapshot(): TenderDigest {
        return TenderDigest(
            code = code,
            title = title,
            score = score,
            pallets = pallets,
            visible = visible,
            routeCode = routeCode,
            expiresAt = expiresAt,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a tender digest in status open. */
fun newTenderDigest(code: String, title: String): TenderDigest {
    return TenderDigest(
        code = code,
        title = title,
        score = 0.0,
        pallets = 0,
        visible = false,
        routeCode = "",
        expiresAt = 0,
        threshold = 0.0,
        status = TenderDigestStatus.OPEN,
    )
}
