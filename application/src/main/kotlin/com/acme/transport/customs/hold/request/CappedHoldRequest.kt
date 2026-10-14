package com.acme.transport.customs.hold.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capped hold request records of customs's hold work, and the rules, stores, and calculations around
// them.

/** Where a capped hold request is in its life cycle. */
enum class CappedHoldRequestStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A capped hold request as the hold team keeps it. */
data class CappedHoldRequest(
    var code: String,
    var title: String,
    var routeCode: String,
    var attributes: MutableMap<String, String>,
    var depot: String,
    var etaMinutes: Int,
    var durationMinutes: Int,
    var source: String,
    var phone: String,
    var shiftCode: String,
    var tags: MutableList<String>,
    var status: CappedHoldRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedHoldRequestStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedHoldRequestStatus): Boolean {
        when (status) {
            CappedHoldRequestStatus.CREATED -> return next == CappedHoldRequestStatus.PUBLISHED || next == CappedHoldRequestStatus.RETIRED
            CappedHoldRequestStatus.VALIDATED -> return next == CappedHoldRequestStatus.PUBLISHED
            CappedHoldRequestStatus.PUBLISHED -> {
                when (next) {
                    CappedHoldRequestStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedHoldRequestStatus) {
        if (!canMoveTo(next)) {
            error("capped hold request $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netLoad(): Double = round((durationMinutes.toDouble() + etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): CappedHoldRequest {
        return CappedHoldRequest(
            code = code,
            title = title,
            routeCode = routeCode,
            attributes = attributes,
            depot = depot,
            etaMinutes = etaMinutes,
            durationMinutes = durationMinutes,
            source = source,
            phone = phone,
            shiftCode = shiftCode,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a capped hold request in status created. */
fun newCappedHoldRequest(code: String, title: String): CappedHoldRequest {
    return CappedHoldRequest(
        code = code,
        title = title,
        routeCode = "",
        attributes = mutableMapOf<String, String>(),
        depot = "",
        etaMinutes = 0,
        durationMinutes = 0,
        source = "",
        phone = "",
        shiftCode = "",
        tags = mutableListOf<String>(),
        status = CappedHoldRequestStatus.CREATED,
    )
}
