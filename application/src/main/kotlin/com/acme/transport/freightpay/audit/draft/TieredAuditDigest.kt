package com.acme.transport.freightpay.audit.draft

import com.acme.transport.kit.text.normalize

/** Where a tiered audit digest is in its life cycle. */
enum class TieredAuditDigestStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a tiered audit digest and its current status. */
data class TieredAuditDigest(
    var code: String,
    var name: String,
    var attributes: MutableMap<String, String>,
    var graceDays: Int,
    var notes: String,
    var status: TieredAuditDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredAuditDigestStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredAuditDigestStatus): Boolean {
        when (status) {
            TieredAuditDigestStatus.IDLE -> return next == TieredAuditDigestStatus.DRAINING || next == TieredAuditDigestStatus.STOPPED
            TieredAuditDigestStatus.WARMING -> {
                when (next) {
                    TieredAuditDigestStatus.DRAINING, TieredAuditDigestStatus.STOPPED -> return true
                }
                return false
            }
            TieredAuditDigestStatus.READY -> {
                when (next) {
                    TieredAuditDigestStatus.DRAINING, TieredAuditDigestStatus.STOPPED -> return true
                }
                return false
            }
            TieredAuditDigestStatus.DRAINING -> {
                when (next) {
                    TieredAuditDigestStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredAuditDigestStatus) {
        if (!canMoveTo(next)) {
            error("tiered audit digest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = graceDays.toDouble()

    fun snapshot(): TieredAuditDigest {
        return TieredAuditDigest(
            code = code,
            name = name,
            attributes = attributes,
            graceDays = graceDays,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a tiered audit digest in status idle. */
fun newTieredAuditDigest(code: String, name: String): TieredAuditDigest {
    return TieredAuditDigest(
        code = code,
        name = name,
        attributes = mutableMapOf<String, String>(),
        graceDays = 0,
        notes = "",
        status = TieredAuditDigestStatus.IDLE,
    )
}
