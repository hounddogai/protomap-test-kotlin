package com.acme.transport.crewing.swap.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped swap policy is in its life cycle. */
enum class CappedSwapPolicyStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One capped swap policy, with what the planners need to act on it. */
data class CappedSwapPolicy(
    var code: String,
    var title: String,
    var contactEmail: String,
    var loadKg: Double,
    var windowDays: Int,
    var timezone: String,
    var name: String,
    var floorValue: Double,
    var startsAt: Int,
    var version: Int,
    var description: String,
    var status: CappedSwapPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedSwapPolicyStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedSwapPolicyStatus): Boolean {
        when (status) {
            CappedSwapPolicyStatus.CREATED -> {
                when (next) {
                    CappedSwapPolicyStatus.PUBLISHED -> return true
                }
                return false
            }
            CappedSwapPolicyStatus.VALIDATED -> return next == CappedSwapPolicyStatus.PUBLISHED || next == CappedSwapPolicyStatus.RETIRED
            CappedSwapPolicyStatus.PUBLISHED -> return next == CappedSwapPolicyStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: CappedSwapPolicyStatus) {
        if (!canMoveTo(next)) {
            error("capped swap policy $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "$code [$title]"

    fun adjustedValue(): Double = round((loadKg - windowDays.toDouble()) * 100) / 100

    fun snapshot(): CappedSwapPolicy {
        return CappedSwapPolicy(
            code = code,
            title = title,
            contactEmail = contactEmail,
            loadKg = loadKg,
            windowDays = windowDays,
            timezone = timezone,
            name = name,
            floorValue = floorValue,
            startsAt = startsAt,
            version = version,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a capped swap policy in status created. */
fun newCappedSwapPolicy(code: String, title: String): CappedSwapPolicy {
    return CappedSwapPolicy(
        code = code,
        title = title,
        contactEmail = "",
        loadKg = 0.0,
        windowDays = 0,
        timezone = "",
        name = "",
        floorValue = 0.0,
        startsAt = 0,
        version = 0,
        description = "",
        status = CappedSwapPolicyStatus.CREATED,
    )
}
