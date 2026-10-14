package com.acme.transport.terminal.crossdock.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a cross dock rule is in its life cycle. */
enum class CrossDockRuleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a cross dock rule and its current status. */
data class CrossDockRule(
    var code: String,
    var name: String,
    var routeCode: String,
    var weight: Double,
    var revision: Int,
    var batchSize: Int,
    var score: Double,
    var shiftCode: String,
    var loadKg: Double,
    var reviewer: String,
    var distanceKm: Double,
    var status: CrossDockRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CrossDockRuleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: CrossDockRuleStatus): Boolean {
        when (status) {
            CrossDockRuleStatus.DRAFT -> {
                when (next) {
                    CrossDockRuleStatus.SUSPENDED -> return true
                }
                return false
            }
            CrossDockRuleStatus.PENDING -> {
                when (next) {
                    CrossDockRuleStatus.ACTIVE, CrossDockRuleStatus.CLOSED -> return true
                }
                return false
            }
            CrossDockRuleStatus.ACTIVE -> return next == CrossDockRuleStatus.SUSPENDED
            CrossDockRuleStatus.SUSPENDED -> return next == CrossDockRuleStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: CrossDockRuleStatus) {
        if (!canMoveTo(next)) {
            error("cross dock rule $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"cross dock rule"} $code"

    fun adjustedLoad(): Double = round(distanceKm * weight * 100) / 100

    fun snapshot(): CrossDockRule {
        return CrossDockRule(
            code = code,
            name = name,
            routeCode = routeCode,
            weight = weight,
            revision = revision,
            batchSize = batchSize,
            score = score,
            shiftCode = shiftCode,
            loadKg = loadKg,
            reviewer = reviewer,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a cross dock rule in status draft. */
fun newCrossDockRule(code: String, name: String): CrossDockRule {
    return CrossDockRule(
        code = code,
        name = name,
        routeCode = "",
        weight = 0.0,
        revision = 0,
        batchSize = 0,
        score = 0.0,
        shiftCode = "",
        loadKg = 0.0,
        reviewer = "",
        distanceKm = 0.0,
        status = CrossDockRuleStatus.DRAFT,
    )
}
