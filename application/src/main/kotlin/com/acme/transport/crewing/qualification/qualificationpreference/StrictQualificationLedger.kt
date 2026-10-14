package com.acme.transport.crewing.qualification.qualificationpreference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict qualification ledger is in its life cycle. */
enum class StrictQualificationLedgerStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A strict qualification ledger. Its code is unique within a tenant. */
data class StrictQualificationLedger(
    var code: String,
    var name: String,
    var segment: String,
    var phone: String,
    var attributes: MutableMap<String, String>,
    var graceDays: Int,
    var limit: Int,
    var tags: MutableList<String>,
    var region: String,
    var quantity: Int,
    var expiresAt: Int,
    var status: StrictQualificationLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictQualificationLedgerStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictQualificationLedgerStatus): Boolean {
        when (status) {
            StrictQualificationLedgerStatus.DRAFT -> {
                when (next) {
                    StrictQualificationLedgerStatus.PENDING, StrictQualificationLedgerStatus.ACTIVE -> return true
                }
                return false
            }
            StrictQualificationLedgerStatus.PENDING -> return next == StrictQualificationLedgerStatus.ACTIVE || next == StrictQualificationLedgerStatus.CLOSED
            StrictQualificationLedgerStatus.ACTIVE -> {
                when (next) {
                    StrictQualificationLedgerStatus.SUSPENDED, StrictQualificationLedgerStatus.CLOSED -> return true
                }
                return false
            }
            StrictQualificationLedgerStatus.SUSPENDED -> {
                when (next) {
                    StrictQualificationLedgerStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictQualificationLedgerStatus) {
        if (!canMoveTo(next)) {
            error("strict qualification ledger $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"strict qualification ledger"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveExposure(): Double = round(expiresAt.toDouble() * limit.toDouble() * 100) / 100

    fun snapshot(): StrictQualificationLedger {
        return StrictQualificationLedger(
            code = code,
            name = name,
            segment = segment,
            phone = phone,
            attributes = attributes,
            graceDays = graceDays,
            limit = limit,
            tags = tags,
            region = region,
            quantity = quantity,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a strict qualification ledger in status draft. */
fun newStrictQualificationLedger(code: String, name: String): StrictQualificationLedger {
    return StrictQualificationLedger(
        code = code,
        name = name,
        segment = "",
        phone = "",
        attributes = mutableMapOf<String, String>(),
        graceDays = 0,
        limit = 0,
        tags = mutableListOf<String>(),
        region = "",
        quantity = 0,
        expiresAt = 0,
        status = StrictQualificationLedgerStatus.DRAFT,
    )
}
