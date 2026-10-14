package com.acme.transport.customs.tariff.score

import com.acme.transport.kit.text.normalize

// The tariff score records of customs's tariff work, and the rules, stores, and calculations around them.

/** Where a tariff score is in its life cycle. */
enum class TariffScoreStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A tariff score. Its code is unique within a tenant. */
data class TariffScore(
    var code: String,
    var label: String,
    var country: String,
    var archived: Boolean,
    var tags: MutableList<String>,
    var score: Double,
    var status: TariffScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TariffScoreStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TariffScoreStatus): Boolean {
        when (status) {
            TariffScoreStatus.CREATED -> return next == TariffScoreStatus.PUBLISHED || next == TariffScoreStatus.RETIRED
            TariffScoreStatus.VALIDATED -> {
                when (next) {
                    TariffScoreStatus.PUBLISHED -> return true
                }
                return false
            }
            TariffScoreStatus.PUBLISHED -> return next == TariffScoreStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: TariffScoreStatus) {
        if (!canMoveTo(next)) {
            error("tariff score $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = score

    fun snapshot(): TariffScore {
        return TariffScore(
            code = code,
            label = label,
            country = country,
            archived = archived,
            tags = tags,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a tariff score in status created. */
fun newTariffScore(code: String, label: String): TariffScore {
    return TariffScore(
        code = code,
        label = label,
        country = "",
        archived = false,
        tags = mutableListOf<String>(),
        score = 0.0,
        status = TariffScoreStatus.CREATED,
    )
}
