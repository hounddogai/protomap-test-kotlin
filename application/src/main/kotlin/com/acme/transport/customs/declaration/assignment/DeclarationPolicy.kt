package com.acme.transport.customs.declaration.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a declaration policy is in its life cycle. */
enum class DeclarationPolicyStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One declaration policy, with what the planners need to act on it. */
data class DeclarationPolicy(
    var code: String,
    var label: String,
    var version: Int,
    var vehicleId: String,
    var endsAt: Int,
    var status: DeclarationPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DeclarationPolicyStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: DeclarationPolicyStatus): Boolean {
        when (status) {
            DeclarationPolicyStatus.NEW -> return next == DeclarationPolicyStatus.RUNNING
            DeclarationPolicyStatus.QUEUED -> return next == DeclarationPolicyStatus.DONE || next == DeclarationPolicyStatus.FAILED
            DeclarationPolicyStatus.RUNNING -> {
                when (next) {
                    DeclarationPolicyStatus.FAILED -> return true
                }
                return false
            }
            DeclarationPolicyStatus.DONE -> {
                when (next) {
                    DeclarationPolicyStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DeclarationPolicyStatus) {
        if (!canMoveTo(next)) {
            error("declaration policy $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun displayLabel(): String = "$code [$label]"

    fun weightedValue(): Double = round((version.toDouble() + endsAt.toDouble()) * 100) / 100

    fun snapshot(): DeclarationPolicy {
        return DeclarationPolicy(
            code = code,
            label = label,
            version = version,
            vehicleId = vehicleId,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a declaration policy in status new. */
fun newDeclarationPolicy(code: String, label: String): DeclarationPolicy {
    return DeclarationPolicy(
        code = code,
        label = label,
        version = 0,
        vehicleId = "",
        endsAt = 0,
        status = DeclarationPolicyStatus.NEW,
    )
}
