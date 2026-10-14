package com.acme.transport.customs.declaration.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global declaration policy is in its life cycle. */
enum class GlobalDeclarationPolicyStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A global declaration policy as the declaration team keeps it. */
data class GlobalDeclarationPolicy(
    var code: String,
    var title: String,
    var windowDays: Int,
    var approved: Boolean,
    var active: Boolean,
    var dock: String,
    var cost: Double,
    var externalRef: String,
    var status: GlobalDeclarationPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalDeclarationPolicyStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalDeclarationPolicyStatus): Boolean {
        when (status) {
            GlobalDeclarationPolicyStatus.IDLE -> {
                when (next) {
                    GlobalDeclarationPolicyStatus.READY, GlobalDeclarationPolicyStatus.STOPPED -> return true
                }
                return false
            }
            GlobalDeclarationPolicyStatus.WARMING -> return next == GlobalDeclarationPolicyStatus.DRAINING || next == GlobalDeclarationPolicyStatus.STOPPED
            GlobalDeclarationPolicyStatus.READY -> {
                when (next) {
                    GlobalDeclarationPolicyStatus.DRAINING -> return true
                }
                return false
            }
            GlobalDeclarationPolicyStatus.DRAINING -> return next == GlobalDeclarationPolicyStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: GlobalDeclarationPolicyStatus) {
        if (!canMoveTo(next)) {
            error("global declaration policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"global declaration policy"} $code"

    fun effectiveExposure(): Double = round((cost - windowDays.toDouble()) * 100) / 100

    fun snapshot(): GlobalDeclarationPolicy {
        return GlobalDeclarationPolicy(
            code = code,
            title = title,
            windowDays = windowDays,
            approved = approved,
            active = active,
            dock = dock,
            cost = cost,
            externalRef = externalRef,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a global declaration policy in status idle. */
fun newGlobalDeclarationPolicy(code: String, title: String): GlobalDeclarationPolicy {
    return GlobalDeclarationPolicy(
        code = code,
        title = title,
        windowDays = 0,
        approved = false,
        active = false,
        dock = "",
        cost = 0.0,
        externalRef = "",
        status = GlobalDeclarationPolicyStatus.IDLE,
    )
}
