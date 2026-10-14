package com.acme.transport.terminal.sortation.draft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered sortation digest is in its life cycle. */
enum class TieredSortationDigestStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One tiered sortation digest, with what the planners need to act on it. */
data class TieredSortationDigest(
    var code: String,
    var title: String,
    var retries: Int,
    var externalRef: String,
    var attempts: Int,
    var segment: String,
    var status: TieredSortationDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredSortationDigestStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredSortationDigestStatus): Boolean {
        when (status) {
            TieredSortationDigestStatus.REQUESTED -> return next == TieredSortationDigestStatus.REVOKED || next == TieredSortationDigestStatus.EXPIRED
            TieredSortationDigestStatus.GRANTED -> return next == TieredSortationDigestStatus.REVOKED || next == TieredSortationDigestStatus.EXPIRED
            TieredSortationDigestStatus.REVOKED -> {
                when (next) {
                    TieredSortationDigestStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredSortationDigestStatus) {
        if (!canMoveTo(next)) {
            error("tiered sortation digest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"tiered sortation digest"} $code"

    fun effectiveTotal(): Double = round((retries.toDouble() - attempts.toDouble()) * 100) / 100

    fun snapshot(): TieredSortationDigest {
        return TieredSortationDigest(
            code = code,
            title = title,
            retries = retries,
            externalRef = externalRef,
            attempts = attempts,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a tiered sortation digest in status requested. */
fun newTieredSortationDigest(code: String, title: String): TieredSortationDigest {
    return TieredSortationDigest(
        code = code,
        title = title,
        retries = 0,
        externalRef = "",
        attempts = 0,
        segment = "",
        status = TieredSortationDigestStatus.REQUESTED,
    )
}
