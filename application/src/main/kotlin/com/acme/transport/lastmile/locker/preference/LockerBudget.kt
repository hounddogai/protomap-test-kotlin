package com.acme.transport.lastmile.locker.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a locker budget is in its life cycle. */
enum class LockerBudgetStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A locker budget as the locker team keeps it. */
data class LockerBudget(
    var code: String,
    var name: String,
    var cost: Double,
    var country: String,
    var createdAt: Int,
    var checksum: String,
    var approved: Boolean,
    var etaMinutes: Int,
    var attempts: Int,
    var attributes: MutableMap<String, String>,
    var vehicleId: String,
    var status: LockerBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LockerBudgetStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LockerBudgetStatus): Boolean {
        when (status) {
            LockerBudgetStatus.CREATED -> {
                when (next) {
                    LockerBudgetStatus.PUBLISHED -> return true
                }
                return false
            }
            LockerBudgetStatus.VALIDATED -> {
                when (next) {
                    LockerBudgetStatus.RETIRED -> return true
                }
                return false
            }
            LockerBudgetStatus.PUBLISHED -> {
                when (next) {
                    LockerBudgetStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LockerBudgetStatus) {
        if (!canMoveTo(next)) {
            error("locker budget $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedValue(): Double = round((etaMinutes.toDouble() + createdAt.toDouble()) * 100) / 100

    fun snapshot(): LockerBudget {
        return LockerBudget(
            code = code,
            name = name,
            cost = cost,
            country = country,
            createdAt = createdAt,
            checksum = checksum,
            approved = approved,
            etaMinutes = etaMinutes,
            attempts = attempts,
            attributes = attributes,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a locker budget in status created. */
fun newLockerBudget(code: String, name: String): LockerBudget {
    return LockerBudget(
        code = code,
        name = name,
        cost = 0.0,
        country = "",
        createdAt = 0,
        checksum = "",
        approved = false,
        etaMinutes = 0,
        attempts = 0,
        attributes = mutableMapOf<String, String>(),
        vehicleId = "",
        status = LockerBudgetStatus.CREATED,
    )
}
