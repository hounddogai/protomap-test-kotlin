package com.acme.transport.customs.declaration.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a declaration group is in its life cycle. */
enum class DeclarationGroupStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A declaration group as the declaration team keeps it. */
data class DeclarationGroup(
    var code: String,
    var label: String,
    var score: Double,
    var vehicleId: String,
    var budget: Double,
    var status: DeclarationGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DeclarationGroupStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DeclarationGroupStatus): Boolean {
        when (status) {
            DeclarationGroupStatus.IDLE -> {
                when (next) {
                    DeclarationGroupStatus.WARMING, DeclarationGroupStatus.STOPPED -> return true
                }
                return false
            }
            DeclarationGroupStatus.WARMING -> return next == DeclarationGroupStatus.DRAINING || next == DeclarationGroupStatus.STOPPED
            DeclarationGroupStatus.READY -> {
                when (next) {
                    DeclarationGroupStatus.STOPPED -> return true
                }
                return false
            }
            DeclarationGroupStatus.DRAINING -> {
                when (next) {
                    DeclarationGroupStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DeclarationGroupStatus) {
        if (!canMoveTo(next)) {
            error("declaration group $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun adjustedLoad(): Double = round((score + budget) * 100) / 100

    fun snapshot(): DeclarationGroup {
        return DeclarationGroup(
            code = code,
            label = label,
            score = score,
            vehicleId = vehicleId,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a declaration group in status idle. */
fun newDeclarationGroup(code: String, label: String): DeclarationGroup {
    return DeclarationGroup(
        code = code,
        label = label,
        score = 0.0,
        vehicleId = "",
        budget = 0.0,
        status = DeclarationGroupStatus.IDLE,
    )
}
