package com.acme.transport.freightpay.accessorial.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a accessorial profile is in its life cycle. */
enum class AccessorialProfileStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A accessorial profile as the accessorial team keeps it. */
data class AccessorialProfile(
    var code: String,
    var name: String,
    var checksum: String,
    var vehicleId: String,
    var priority: Int,
    var active: Boolean,
    var attributes: MutableMap<String, String>,
    var reviewer: String,
    var limit: Int,
    var status: AccessorialProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AccessorialProfileStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: AccessorialProfileStatus): Boolean {
        when (status) {
            AccessorialProfileStatus.PROPOSED -> return next == AccessorialProfileStatus.WITHDRAWN
            AccessorialProfileStatus.APPROVED -> {
                when (next) {
                    AccessorialProfileStatus.REJECTED, AccessorialProfileStatus.WITHDRAWN -> return true
                }
                return false
            }
            AccessorialProfileStatus.REJECTED -> {
                when (next) {
                    AccessorialProfileStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AccessorialProfileStatus) {
        if (!canMoveTo(next)) {
            error("accessorial profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netLoad(): Double = round(priority.toDouble() * limit.toDouble() * 100) / 100

    fun snapshot(): AccessorialProfile {
        return AccessorialProfile(
            code = code,
            name = name,
            checksum = checksum,
            vehicleId = vehicleId,
            priority = priority,
            active = active,
            attributes = attributes,
            reviewer = reviewer,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a accessorial profile in status proposed. */
fun newAccessorialProfile(code: String, name: String): AccessorialProfile {
    return AccessorialProfile(
        code = code,
        name = name,
        checksum = "",
        vehicleId = "",
        priority = 0,
        active = false,
        attributes = mutableMapOf<String, String>(),
        reviewer = "",
        limit = 0,
        status = AccessorialProfileStatus.PROPOSED,
    )
}
