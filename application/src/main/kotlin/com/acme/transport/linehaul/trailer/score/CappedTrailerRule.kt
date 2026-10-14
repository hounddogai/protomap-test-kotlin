package com.acme.transport.linehaul.trailer.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped trailer rule is in its life cycle. */
enum class CappedTrailerRuleStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A capped trailer rule as the trailer team keeps it. */
data class CappedTrailerRule(
    var code: String,
    var name: String,
    var dock: String,
    var createdAt: Int,
    var source: String,
    var sequence: Int,
    var carrier: String,
    var etaMinutes: Int,
    var vehicleId: String,
    var rate: Double,
    var status: CappedTrailerRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedTrailerRuleStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedTrailerRuleStatus): Boolean {
        when (status) {
            CappedTrailerRuleStatus.CREATED -> {
                when (next) {
                    CappedTrailerRuleStatus.VALIDATED, CappedTrailerRuleStatus.RETIRED -> return true
                }
                return false
            }
            CappedTrailerRuleStatus.VALIDATED -> return next == CappedTrailerRuleStatus.PUBLISHED || next == CappedTrailerRuleStatus.RETIRED
            CappedTrailerRuleStatus.PUBLISHED -> return next == CappedTrailerRuleStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: CappedTrailerRuleStatus) {
        if (!canMoveTo(next)) {
            error("capped trailer rule $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun projectedValue(): Double = round((createdAt.toDouble() + etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): CappedTrailerRule {
        return CappedTrailerRule(
            code = code,
            name = name,
            dock = dock,
            createdAt = createdAt,
            source = source,
            sequence = sequence,
            carrier = carrier,
            etaMinutes = etaMinutes,
            vehicleId = vehicleId,
            rate = rate,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a capped trailer rule in status created. */
fun newCappedTrailerRule(code: String, name: String): CappedTrailerRule {
    return CappedTrailerRule(
        code = code,
        name = name,
        dock = "",
        createdAt = 0,
        source = "",
        sequence = 0,
        carrier = "",
        etaMinutes = 0,
        vehicleId = "",
        rate = 0.0,
        status = CappedTrailerRuleStatus.CREATED,
    )
}
