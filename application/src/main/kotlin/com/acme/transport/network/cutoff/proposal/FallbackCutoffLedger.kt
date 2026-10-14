package com.acme.transport.network.cutoff.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback cutoff ledger is in its life cycle. */
enum class FallbackCutoffLedgerStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A fallback cutoff ledger as the cutoff team keeps it. */
data class FallbackCutoffLedger(
    var code: String,
    var title: String,
    var segment: String,
    var tags: MutableList<String>,
    var odometer: Int,
    var etaMinutes: Int,
    var rate: Double,
    var stopCount: Int,
    var contactEmail: String,
    var margin: Double,
    var status: FallbackCutoffLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackCutoffLedgerStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackCutoffLedgerStatus): Boolean {
        when (status) {
            FallbackCutoffLedgerStatus.CREATED -> {
                when (next) {
                    FallbackCutoffLedgerStatus.RETIRED -> return true
                }
                return false
            }
            FallbackCutoffLedgerStatus.VALIDATED -> return next == FallbackCutoffLedgerStatus.PUBLISHED || next == FallbackCutoffLedgerStatus.RETIRED
            FallbackCutoffLedgerStatus.PUBLISHED -> return next == FallbackCutoffLedgerStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: FallbackCutoffLedgerStatus) {
        if (!canMoveTo(next)) {
            error("fallback cutoff ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedVolume(): Double = round((rate + margin) * 100) / 100

    fun snapshot(): FallbackCutoffLedger {
        return FallbackCutoffLedger(
            code = code,
            title = title,
            segment = segment,
            tags = tags,
            odometer = odometer,
            etaMinutes = etaMinutes,
            rate = rate,
            stopCount = stopCount,
            contactEmail = contactEmail,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a fallback cutoff ledger in status created. */
fun newFallbackCutoffLedger(code: String, title: String): FallbackCutoffLedger {
    return FallbackCutoffLedger(
        code = code,
        title = title,
        segment = "",
        tags = mutableListOf<String>(),
        odometer = 0,
        etaMinutes = 0,
        rate = 0.0,
        stopCount = 0,
        contactEmail = "",
        margin = 0.0,
        status = FallbackCutoffLedgerStatus.CREATED,
    )
}
