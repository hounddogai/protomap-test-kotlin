package com.acme.transport.linehaul.tender.review

import com.acme.transport.kit.text.normalize

/** Where a tiered tender budget is in its life cycle. */
enum class TieredTenderBudgetStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One tiered tender budget, with what the planners need to act on it. */
data class TieredTenderBudget(
    var code: String,
    var name: String,
    var capacity: Int,
    var phone: String,
    var attributes: MutableMap<String, String>,
    var status: TieredTenderBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredTenderBudgetStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredTenderBudgetStatus): Boolean {
        when (status) {
            TieredTenderBudgetStatus.CREATED -> return next == TieredTenderBudgetStatus.PUBLISHED
            TieredTenderBudgetStatus.VALIDATED -> {
                when (next) {
                    TieredTenderBudgetStatus.PUBLISHED, TieredTenderBudgetStatus.RETIRED -> return true
                }
                return false
            }
            TieredTenderBudgetStatus.PUBLISHED -> return next == TieredTenderBudgetStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: TieredTenderBudgetStatus) {
        if (!canMoveTo(next)) {
            error("tiered tender budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = capacity.toDouble()

    fun snapshot(): TieredTenderBudget {
        return TieredTenderBudget(
            code = code,
            name = name,
            capacity = capacity,
            phone = phone,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a tiered tender budget in status created. */
fun newTieredTenderBudget(code: String, name: String): TieredTenderBudget {
    return TieredTenderBudget(
        code = code,
        name = name,
        capacity = 0,
        phone = "",
        attributes = mutableMapOf<String, String>(),
        status = TieredTenderBudgetStatus.CREATED,
    )
}
