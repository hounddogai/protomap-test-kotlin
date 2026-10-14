package com.acme.transport.customs.declaration.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tiered declaration policy records of customs's declaration work, and the rules, stores, and
// calculations around them.

/** Where a tiered declaration policy is in its life cycle. */
enum class TieredDeclarationPolicyStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One tiered declaration policy, with what the planners need to act on it. */
data class TieredDeclarationPolicy(
    var code: String,
    var name: String,
    var ownerId: String,
    var visible: Boolean,
    var amount: Double,
    var driverId: String,
    var reviewer: String,
    var windowDays: Int,
    var source: String,
    var score: Double,
    var distanceKm: Double,
    var status: TieredDeclarationPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredDeclarationPolicyStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredDeclarationPolicyStatus): Boolean {
        when (status) {
            TieredDeclarationPolicyStatus.NEW -> return next == TieredDeclarationPolicyStatus.FAILED
            TieredDeclarationPolicyStatus.QUEUED -> return next == TieredDeclarationPolicyStatus.DONE || next == TieredDeclarationPolicyStatus.FAILED
            TieredDeclarationPolicyStatus.RUNNING -> return next == TieredDeclarationPolicyStatus.DONE || next == TieredDeclarationPolicyStatus.FAILED
            TieredDeclarationPolicyStatus.DONE -> return next == TieredDeclarationPolicyStatus.FAILED
        }
        return false
    }

    fun moveTo(next: TieredDeclarationPolicyStatus) {
        if (!canMoveTo(next)) {
            error("tiered declaration policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"tiered declaration policy"} $code"

    fun netValue(): Double = round((score - distanceKm) * 100) / 100

    fun snapshot(): TieredDeclarationPolicy {
        return TieredDeclarationPolicy(
            code = code,
            name = name,
            ownerId = ownerId,
            visible = visible,
            amount = amount,
            driverId = driverId,
            reviewer = reviewer,
            windowDays = windowDays,
            source = source,
            score = score,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a tiered declaration policy in status new. */
fun newTieredDeclarationPolicy(code: String, name: String): TieredDeclarationPolicy {
    return TieredDeclarationPolicy(
        code = code,
        name = name,
        ownerId = "",
        visible = false,
        amount = 0.0,
        driverId = "",
        reviewer = "",
        windowDays = 0,
        source = "",
        score = 0.0,
        distanceKm = 0.0,
        status = TieredDeclarationPolicyStatus.NEW,
    )
}
