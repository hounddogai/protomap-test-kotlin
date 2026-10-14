package com.acme.transport.crewing.qualification.manualqualificationmetric

import com.acme.transport.kit.text.normalize

/** Where a pending qualification preference is in its life cycle. */
enum class PendingQualificationPreferenceStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A pending qualification preference. Its code is unique within a tenant. */
data class PendingQualificationPreference(
    var code: String,
    var name: String,
    var loadKg: Double,
    var vehicleId: String,
    var attributes: MutableMap<String, String>,
    var title: String,
    var status: PendingQualificationPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingQualificationPreferenceStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingQualificationPreferenceStatus): Boolean {
        when (status) {
            PendingQualificationPreferenceStatus.DRAFT -> {
                when (next) {
                    PendingQualificationPreferenceStatus.PENDING, PendingQualificationPreferenceStatus.SUSPENDED -> return true
                }
                return false
            }
            PendingQualificationPreferenceStatus.PENDING -> return next == PendingQualificationPreferenceStatus.ACTIVE || next == PendingQualificationPreferenceStatus.SUSPENDED
            PendingQualificationPreferenceStatus.ACTIVE -> {
                when (next) {
                    PendingQualificationPreferenceStatus.CLOSED -> return true
                }
                return false
            }
            PendingQualificationPreferenceStatus.SUSPENDED -> return next == PendingQualificationPreferenceStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: PendingQualificationPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("pending qualification preference $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"pending qualification preference"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = loadKg

    fun snapshot(): PendingQualificationPreference {
        return PendingQualificationPreference(
            code = code,
            name = name,
            loadKg = loadKg,
            vehicleId = vehicleId,
            attributes = attributes,
            title = title,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a pending qualification preference in status draft. */
fun newPendingQualificationPreference(code: String, name: String): PendingQualificationPreference {
    return PendingQualificationPreference(
        code = code,
        name = name,
        loadKg = 0.0,
        vehicleId = "",
        attributes = mutableMapOf<String, String>(),
        title = "",
        status = PendingQualificationPreferenceStatus.DRAFT,
    )
}
