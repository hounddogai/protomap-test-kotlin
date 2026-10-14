package com.acme.transport.terminal.appointment.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy appointment digest is in its life cycle. */
enum class LegacyAppointmentDigestStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A legacy appointment digest. Its code is unique within a tenant. */
data class LegacyAppointmentDigest(
    var code: String,
    var name: String,
    var source: String,
    var updatedAt: Int,
    var fuelLiters: Double,
    var externalRef: String,
    var createdAt: Int,
    var margin: Double,
    var score: Double,
    var revision: Int,
    var status: LegacyAppointmentDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyAppointmentDigestStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyAppointmentDigestStatus): Boolean {
        when (status) {
            LegacyAppointmentDigestStatus.DRAFT -> {
                when (next) {
                    LegacyAppointmentDigestStatus.PENDING, LegacyAppointmentDigestStatus.SUSPENDED -> return true
                }
                return false
            }
            LegacyAppointmentDigestStatus.PENDING -> return next == LegacyAppointmentDigestStatus.ACTIVE
            LegacyAppointmentDigestStatus.ACTIVE -> {
                when (next) {
                    LegacyAppointmentDigestStatus.CLOSED -> return true
                }
                return false
            }
            LegacyAppointmentDigestStatus.SUSPENDED -> {
                when (next) {
                    LegacyAppointmentDigestStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyAppointmentDigestStatus) {
        if (!canMoveTo(next)) {
            error("legacy appointment digest $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code [$name]"

    fun netExposure(): Double = round((score - margin) * 100) / 100

    fun snapshot(): LegacyAppointmentDigest {
        return LegacyAppointmentDigest(
            code = code,
            name = name,
            source = source,
            updatedAt = updatedAt,
            fuelLiters = fuelLiters,
            externalRef = externalRef,
            createdAt = createdAt,
            margin = margin,
            score = score,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a legacy appointment digest in status draft. */
fun newLegacyAppointmentDigest(code: String, name: String): LegacyAppointmentDigest {
    return LegacyAppointmentDigest(
        code = code,
        name = name,
        source = "",
        updatedAt = 0,
        fuelLiters = 0.0,
        externalRef = "",
        createdAt = 0,
        margin = 0.0,
        score = 0.0,
        revision = 0,
        status = LegacyAppointmentDigestStatus.DRAFT,
    )
}
