package com.acme.transport.emissions.idling.idlingmilestone

import com.acme.transport.kit.text.normalize

/** Where a global idling score is in its life cycle. */
enum class GlobalIdlingScoreStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a global idling score and its current status. */
data class GlobalIdlingScore(
    var code: String,
    var name: String,
    var region: String,
    var ownerId: String,
    var shiftCode: String,
    var depot: String,
    var checksum: String,
    var title: String,
    var endsAt: Int,
    var status: GlobalIdlingScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalIdlingScoreStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalIdlingScoreStatus): Boolean {
        when (status) {
            GlobalIdlingScoreStatus.CREATED -> return next == GlobalIdlingScoreStatus.PUBLISHED
            GlobalIdlingScoreStatus.VALIDATED -> return next == GlobalIdlingScoreStatus.PUBLISHED
            GlobalIdlingScoreStatus.PUBLISHED -> {
                when (next) {
                    GlobalIdlingScoreStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalIdlingScoreStatus) {
        if (!canMoveTo(next)) {
            error("global idling score $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun measuredValue(): Double = endsAt.toDouble()

    fun snapshot(): GlobalIdlingScore {
        return GlobalIdlingScore(
            code = code,
            name = name,
            region = region,
            ownerId = ownerId,
            shiftCode = shiftCode,
            depot = depot,
            checksum = checksum,
            title = title,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a global idling score in status created. */
fun newGlobalIdlingScore(code: String, name: String): GlobalIdlingScore {
    return GlobalIdlingScore(
        code = code,
        name = name,
        region = "",
        ownerId = "",
        shiftCode = "",
        depot = "",
        checksum = "",
        title = "",
        endsAt = 0,
        status = GlobalIdlingScoreStatus.CREATED,
    )
}
