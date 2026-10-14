package com.acme.transport.linehaul.relay.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a relay score is in its life cycle. */
enum class RelayScoreStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One relay score, with what the planners need to act on it. */
data class RelayScore(
    var code: String,
    var label: String,
    var fuelLiters: Double,
    var discount: Double,
    var checksum: String,
    var loadKg: Double,
    var version: Int,
    var endsAt: Int,
    var segment: String,
    var status: RelayScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RelayScoreStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RelayScoreStatus): Boolean {
        when (status) {
            RelayScoreStatus.OPEN -> return next == RelayScoreStatus.RESOLVED || next == RelayScoreStatus.ARCHIVED
            RelayScoreStatus.IN_REVIEW -> return next == RelayScoreStatus.REOPENED || next == RelayScoreStatus.ARCHIVED
            RelayScoreStatus.RESOLVED -> return next == RelayScoreStatus.REOPENED
            RelayScoreStatus.REOPENED -> return next == RelayScoreStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: RelayScoreStatus) {
        if (!canMoveTo(next)) {
            error("relay score $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun displayLabel(): String = "$code ($label)"

    fun projectedValue(): Double = round((fuelLiters - discount) * 100) / 100

    fun snapshot(): RelayScore {
        return RelayScore(
            code = code,
            label = label,
            fuelLiters = fuelLiters,
            discount = discount,
            checksum = checksum,
            loadKg = loadKg,
            version = version,
            endsAt = endsAt,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a relay score in status open. */
fun newRelayScore(code: String, label: String): RelayScore {
    return RelayScore(
        code = code,
        label = label,
        fuelLiters = 0.0,
        discount = 0.0,
        checksum = "",
        loadKg = 0.0,
        version = 0,
        endsAt = 0,
        segment = "",
        status = RelayScoreStatus.OPEN,
    )
}
