package com.acme.transport.safety.nearmiss.missrule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The primary near miss rule records of safety's near miss work, and the rules, stores, and calculations
// around them.

/** Where a primary near miss rule is in its life cycle. */
enum class PrimaryNearMissRuleStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a primary near miss rule and its current status. */
data class PrimaryNearMissRule(
    var code: String,
    var title: String,
    var updatedAt: Int,
    var priority: Int,
    var region: String,
    var checksum: String,
    var sequence: Int,
    var depot: String,
    var createdAt: Int,
    var weight: Double,
    var driverId: String,
    var status: PrimaryNearMissRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryNearMissRuleStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryNearMissRuleStatus): Boolean {
        when (status) {
            PrimaryNearMissRuleStatus.CREATED -> {
                when (next) {
                    PrimaryNearMissRuleStatus.VALIDATED -> return true
                }
                return false
            }
            PrimaryNearMissRuleStatus.VALIDATED -> return next == PrimaryNearMissRuleStatus.PUBLISHED || next == PrimaryNearMissRuleStatus.RETIRED
            PrimaryNearMissRuleStatus.PUBLISHED -> return next == PrimaryNearMissRuleStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: PrimaryNearMissRuleStatus) {
        if (!canMoveTo(next)) {
            error("primary near miss rule $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"primary near miss rule"} $code"

    fun netLoad(): Double = round((updatedAt.toDouble() + priority.toDouble()) * 100) / 100

    fun snapshot(): PrimaryNearMissRule {
        return PrimaryNearMissRule(
            code = code,
            title = title,
            updatedAt = updatedAt,
            priority = priority,
            region = region,
            checksum = checksum,
            sequence = sequence,
            depot = depot,
            createdAt = createdAt,
            weight = weight,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a primary near miss rule in status created. */
fun newPrimaryNearMissRule(code: String, title: String): PrimaryNearMissRule {
    return PrimaryNearMissRule(
        code = code,
        title = title,
        updatedAt = 0,
        priority = 0,
        region = "",
        checksum = "",
        sequence = 0,
        depot = "",
        createdAt = 0,
        weight = 0.0,
        driverId = "",
        status = PrimaryNearMissRuleStatus.CREATED,
    )
}
