package com.acme.transport.safety.coaching.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The manual coaching baseline records of safety's coaching work, and the rules, stores, and calculations
// around them.

/** Where a manual coaching baseline is in its life cycle. */
enum class ManualCoachingBaselineStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A manual coaching baseline. Its code is unique within a tenant. */
data class ManualCoachingBaseline(
    var code: String,
    var name: String,
    var quantity: Int,
    var weight: Double,
    var score: Double,
    var expiresAt: Int,
    var revision: Int,
    var floorValue: Double,
    var ceiling: Double,
    var source: String,
    var status: ManualCoachingBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualCoachingBaselineStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualCoachingBaselineStatus): Boolean {
        when (status) {
            ManualCoachingBaselineStatus.NEW -> return next == ManualCoachingBaselineStatus.QUEUED
            ManualCoachingBaselineStatus.QUEUED -> {
                when (next) {
                    ManualCoachingBaselineStatus.FAILED -> return true
                }
                return false
            }
            ManualCoachingBaselineStatus.RUNNING -> return next == ManualCoachingBaselineStatus.FAILED
            ManualCoachingBaselineStatus.DONE -> {
                when (next) {
                    ManualCoachingBaselineStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualCoachingBaselineStatus) {
        if (!canMoveTo(next)) {
            error("manual coaching baseline $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code ($name)"

    fun adjustedLoad(): Double = round((score - quantity.toDouble()) * 100) / 100

    fun snapshot(): ManualCoachingBaseline {
        return ManualCoachingBaseline(
            code = code,
            name = name,
            quantity = quantity,
            weight = weight,
            score = score,
            expiresAt = expiresAt,
            revision = revision,
            floorValue = floorValue,
            ceiling = ceiling,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a manual coaching baseline in status new. */
fun newManualCoachingBaseline(code: String, name: String): ManualCoachingBaseline {
    return ManualCoachingBaseline(
        code = code,
        name = name,
        quantity = 0,
        weight = 0.0,
        score = 0.0,
        expiresAt = 0,
        revision = 0,
        floorValue = 0.0,
        ceiling = 0.0,
        source = "",
        status = ManualCoachingBaselineStatus.NEW,
    )
}
