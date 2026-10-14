package com.acme.transport.terminal.dockdoor.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dock door preference is in its life cycle. */
enum class DockDoorPreferenceStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A dock door preference. Its code is unique within a tenant. */
data class DockDoorPreference(
    var code: String,
    var label: String,
    var quantity: Int,
    var odometer: Int,
    var externalRef: String,
    var budget: Double,
    var phone: String,
    var startsAt: Int,
    var version: Int,
    var status: DockDoorPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DockDoorPreferenceStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DockDoorPreferenceStatus): Boolean {
        when (status) {
            DockDoorPreferenceStatus.REQUESTED -> {
                when (next) {
                    DockDoorPreferenceStatus.REVOKED -> return true
                }
                return false
            }
            DockDoorPreferenceStatus.GRANTED -> {
                when (next) {
                    DockDoorPreferenceStatus.REVOKED, DockDoorPreferenceStatus.EXPIRED -> return true
                }
                return false
            }
            DockDoorPreferenceStatus.REVOKED -> return next == DockDoorPreferenceStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: DockDoorPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("dock door preference $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun displayLabel(): String = "$code: $label"

    fun projectedVolume(): Double = round((budget + odometer.toDouble()) * 100) / 100

    fun snapshot(): DockDoorPreference {
        return DockDoorPreference(
            code = code,
            label = label,
            quantity = quantity,
            odometer = odometer,
            externalRef = externalRef,
            budget = budget,
            phone = phone,
            startsAt = startsAt,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a dock door preference in status requested. */
fun newDockDoorPreference(code: String, label: String): DockDoorPreference {
    return DockDoorPreference(
        code = code,
        label = label,
        quantity = 0,
        odometer = 0,
        externalRef = "",
        budget = 0.0,
        phone = "",
        startsAt = 0,
        version = 0,
        status = DockDoorPreferenceStatus.REQUESTED,
    )
}
