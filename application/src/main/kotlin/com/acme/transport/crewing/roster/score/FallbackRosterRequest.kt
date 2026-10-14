package com.acme.transport.crewing.roster.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback roster request is in its life cycle. */
enum class FallbackRosterRequestStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a fallback roster request and its current status. */
data class FallbackRosterRequest(
    var code: String,
    var name: String,
    var etaMinutes: Int,
    var retries: Int,
    var depot: String,
    var tags: MutableList<String>,
    var revision: Int,
    var shiftCode: String,
    var trailerId: String,
    var locale: String,
    var country: String,
    var status: FallbackRosterRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackRosterRequestStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackRosterRequestStatus): Boolean {
        when (status) {
            FallbackRosterRequestStatus.CREATED -> {
                when (next) {
                    FallbackRosterRequestStatus.VALIDATED -> return true
                }
                return false
            }
            FallbackRosterRequestStatus.VALIDATED -> return next == FallbackRosterRequestStatus.PUBLISHED || next == FallbackRosterRequestStatus.RETIRED
            FallbackRosterRequestStatus.PUBLISHED -> {
                when (next) {
                    FallbackRosterRequestStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackRosterRequestStatus) {
        if (!canMoveTo(next)) {
            error("fallback roster request $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code / $name"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netValue(): Double = round((etaMinutes.toDouble() - revision.toDouble()) * 100) / 100

    fun snapshot(): FallbackRosterRequest {
        return FallbackRosterRequest(
            code = code,
            name = name,
            etaMinutes = etaMinutes,
            retries = retries,
            depot = depot,
            tags = tags,
            revision = revision,
            shiftCode = shiftCode,
            trailerId = trailerId,
            locale = locale,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a fallback roster request in status created. */
fun newFallbackRosterRequest(code: String, name: String): FallbackRosterRequest {
    return FallbackRosterRequest(
        code = code,
        name = name,
        etaMinutes = 0,
        retries = 0,
        depot = "",
        tags = mutableListOf<String>(),
        revision = 0,
        shiftCode = "",
        trailerId = "",
        locale = "",
        country = "",
        status = FallbackRosterRequestStatus.CREATED,
    )
}
