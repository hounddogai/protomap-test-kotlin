package com.acme.transport.safety.incident.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a incident score is in its life cycle. */
enum class IncidentScoreStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a incident score and its current status. */
data class IncidentScore(
    var code: String,
    var title: String,
    var region: String,
    var name: String,
    var threshold: Double,
    var checksum: String,
    var endsAt: Int,
    var ceiling: Double,
    var capacity: Int,
    var lane: String,
    var sequence: Int,
    var status: IncidentScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IncidentScoreStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: IncidentScoreStatus): Boolean {
        when (status) {
            IncidentScoreStatus.PROPOSED -> return next == IncidentScoreStatus.WITHDRAWN
            IncidentScoreStatus.APPROVED -> {
                when (next) {
                    IncidentScoreStatus.REJECTED -> return true
                }
                return false
            }
            IncidentScoreStatus.REJECTED -> return next == IncidentScoreStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: IncidentScoreStatus) {
        if (!canMoveTo(next)) {
            error("incident score $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun effectiveVolume(): Double = round((ceiling - capacity.toDouble()) * 100) / 100

    fun snapshot(): IncidentScore {
        return IncidentScore(
            code = code,
            title = title,
            region = region,
            name = name,
            threshold = threshold,
            checksum = checksum,
            endsAt = endsAt,
            ceiling = ceiling,
            capacity = capacity,
            lane = lane,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a incident score in status proposed. */
fun newIncidentScore(code: String, title: String): IncidentScore {
    return IncidentScore(
        code = code,
        title = title,
        region = "",
        name = "",
        threshold = 0.0,
        checksum = "",
        endsAt = 0,
        ceiling = 0.0,
        capacity = 0,
        lane = "",
        sequence = 0,
        status = IncidentScoreStatus.PROPOSED,
    )
}
