package com.acme.transport.terminal.sortation.draft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The sortation draft records of terminal's sortation work, and the rules, stores, and calculations around
// them.

/** Where a sortation draft is in its life cycle. */
enum class SortationDraftStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A sortation draft. Its code is unique within a tenant. */
data class SortationDraft(
    var code: String,
    var name: String,
    var phone: String,
    var country: String,
    var category: String,
    var retries: Int,
    var rate: Double,
    var channel: String,
    var locale: String,
    var revision: Int,
    var fuelLiters: Double,
    var status: SortationDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SortationDraftStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: SortationDraftStatus): Boolean {
        when (status) {
            SortationDraftStatus.PROPOSED -> {
                when (next) {
                    SortationDraftStatus.APPROVED, SortationDraftStatus.WITHDRAWN -> return true
                }
                return false
            }
            SortationDraftStatus.APPROVED -> return next == SortationDraftStatus.REJECTED || next == SortationDraftStatus.WITHDRAWN
            SortationDraftStatus.REJECTED -> return next == SortationDraftStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: SortationDraftStatus) {
        if (!canMoveTo(next)) {
            error("sortation draft $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"sortation draft"} $code"

    fun weightedValue(): Double = round(retries.toDouble() * fuelLiters * 100) / 100

    fun snapshot(): SortationDraft {
        return SortationDraft(
            code = code,
            name = name,
            phone = phone,
            country = country,
            category = category,
            retries = retries,
            rate = rate,
            channel = channel,
            locale = locale,
            revision = revision,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a sortation draft in status proposed. */
fun newSortationDraft(code: String, name: String): SortationDraft {
    return SortationDraft(
        code = code,
        name = name,
        phone = "",
        country = "",
        category = "",
        retries = 0,
        rate = 0.0,
        channel = "",
        locale = "",
        revision = 0,
        fuelLiters = 0.0,
        status = SortationDraftStatus.PROPOSED,
    )
}
