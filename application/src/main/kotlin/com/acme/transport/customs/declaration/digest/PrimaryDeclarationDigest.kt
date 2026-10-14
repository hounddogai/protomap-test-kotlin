package com.acme.transport.customs.declaration.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The primary declaration digest records of customs's declaration work, and the rules, stores, and
// calculations around them.

/** Where a primary declaration digest is in its life cycle. */
enum class PrimaryDeclarationDigestStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One primary declaration digest, with what the planners need to act on it. */
data class PrimaryDeclarationDigest(
    var code: String,
    var title: String,
    var etaMinutes: Int,
    var carrier: String,
    var minimum: Double,
    var margin: Double,
    var label: String,
    var ratio: Double,
    var endsAt: Int,
    var pinned: Boolean,
    var status: PrimaryDeclarationDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryDeclarationDigestStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryDeclarationDigestStatus): Boolean {
        when (status) {
            PrimaryDeclarationDigestStatus.OPEN -> return next == PrimaryDeclarationDigestStatus.IN_REVIEW || next == PrimaryDeclarationDigestStatus.ARCHIVED
            PrimaryDeclarationDigestStatus.IN_REVIEW -> return next == PrimaryDeclarationDigestStatus.REOPENED
            PrimaryDeclarationDigestStatus.RESOLVED -> return next == PrimaryDeclarationDigestStatus.REOPENED
            PrimaryDeclarationDigestStatus.REOPENED -> {
                when (next) {
                    PrimaryDeclarationDigestStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryDeclarationDigestStatus) {
        if (!canMoveTo(next)) {
            error("primary declaration digest $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"primary declaration digest"} $code"

    fun effectiveVolume(): Double = round(etaMinutes.toDouble() * endsAt.toDouble() * 100) / 100

    fun snapshot(): PrimaryDeclarationDigest {
        return PrimaryDeclarationDigest(
            code = code,
            title = title,
            etaMinutes = etaMinutes,
            carrier = carrier,
            minimum = minimum,
            margin = margin,
            label = label,
            ratio = ratio,
            endsAt = endsAt,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a primary declaration digest in status open. */
fun newPrimaryDeclarationDigest(code: String, title: String): PrimaryDeclarationDigest {
    return PrimaryDeclarationDigest(
        code = code,
        title = title,
        etaMinutes = 0,
        carrier = "",
        minimum = 0.0,
        margin = 0.0,
        label = "",
        ratio = 0.0,
        endsAt = 0,
        pinned = false,
        status = PrimaryDeclarationDigestStatus.OPEN,
    )
}
