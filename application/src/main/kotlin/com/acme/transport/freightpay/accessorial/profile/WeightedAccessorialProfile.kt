package com.acme.transport.freightpay.accessorial.profile

import com.acme.transport.kit.text.normalize

// The weighted accessorial profile records of freightpay's accessorial work, and the rules, stores, and
// calculations around them.

/** Where a weighted accessorial profile is in its life cycle. */
enum class WeightedAccessorialProfileStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A weighted accessorial profile. Its code is unique within a tenant. */
data class WeightedAccessorialProfile(
    var code: String,
    var name: String,
    var vehicleId: String,
    var attributes: MutableMap<String, String>,
    var summary: String,
    var retries: Int,
    var phone: String,
    var status: WeightedAccessorialProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedAccessorialProfileStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedAccessorialProfileStatus): Boolean {
        when (status) {
            WeightedAccessorialProfileStatus.NEW -> return next == WeightedAccessorialProfileStatus.QUEUED || next == WeightedAccessorialProfileStatus.FAILED
            WeightedAccessorialProfileStatus.QUEUED -> {
                when (next) {
                    WeightedAccessorialProfileStatus.DONE -> return true
                }
                return false
            }
            WeightedAccessorialProfileStatus.RUNNING -> {
                when (next) {
                    WeightedAccessorialProfileStatus.FAILED -> return true
                }
                return false
            }
            WeightedAccessorialProfileStatus.DONE -> return next == WeightedAccessorialProfileStatus.FAILED
        }
        return false
    }

    fun moveTo(next: WeightedAccessorialProfileStatus) {
        if (!canMoveTo(next)) {
            error("weighted accessorial profile $code cannot move from $status to $next")
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

    fun measuredValue(): Double = retries.toDouble()

    fun snapshot(): WeightedAccessorialProfile {
        return WeightedAccessorialProfile(
            code = code,
            name = name,
            vehicleId = vehicleId,
            attributes = attributes,
            summary = summary,
            retries = retries,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a weighted accessorial profile in status new. */
fun newWeightedAccessorialProfile(code: String, name: String): WeightedAccessorialProfile {
    return WeightedAccessorialProfile(
        code = code,
        name = name,
        vehicleId = "",
        attributes = mutableMapOf<String, String>(),
        summary = "",
        retries = 0,
        phone = "",
        status = WeightedAccessorialProfileStatus.NEW,
    )
}
