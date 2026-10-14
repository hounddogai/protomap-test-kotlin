package com.acme.transport.network.cutoff.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a cutoff plan is in its life cycle. */
enum class CutoffPlanStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a cutoff plan and its current status. */
data class CutoffPlan(
    var code: String,
    var title: String,
    var source: String,
    var attributes: MutableMap<String, String>,
    var budget: Double,
    var routeCode: String,
    var cost: Double,
    var phone: String,
    var checksum: String,
    var pinned: Boolean,
    var archived: Boolean,
    var status: CutoffPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CutoffPlanStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: CutoffPlanStatus): Boolean {
        when (status) {
            CutoffPlanStatus.NEW -> {
                when (next) {
                    CutoffPlanStatus.QUEUED -> return true
                }
                return false
            }
            CutoffPlanStatus.QUEUED -> {
                when (next) {
                    CutoffPlanStatus.FAILED -> return true
                }
                return false
            }
            CutoffPlanStatus.RUNNING -> {
                when (next) {
                    CutoffPlanStatus.DONE, CutoffPlanStatus.FAILED -> return true
                }
                return false
            }
            CutoffPlanStatus.DONE -> return next == CutoffPlanStatus.FAILED
        }
        return false
    }

    fun moveTo(next: CutoffPlanStatus) {
        if (!canMoveTo(next)) {
            error("cutoff plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedLoad(): Double = round((cost - budget) * 100) / 100

    fun snapshot(): CutoffPlan {
        return CutoffPlan(
            code = code,
            title = title,
            source = source,
            attributes = attributes,
            budget = budget,
            routeCode = routeCode,
            cost = cost,
            phone = phone,
            checksum = checksum,
            pinned = pinned,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a cutoff plan in status new. */
fun newCutoffPlan(code: String, title: String): CutoffPlan {
    return CutoffPlan(
        code = code,
        title = title,
        source = "",
        attributes = mutableMapOf<String, String>(),
        budget = 0.0,
        routeCode = "",
        cost = 0.0,
        phone = "",
        checksum = "",
        pinned = false,
        archived = false,
        status = CutoffPlanStatus.NEW,
    )
}
