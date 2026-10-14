package com.acme.transport.safety.coaching.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default coaching agreement is in its life cycle. */
enum class DefaultCoachingAgreementStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A default coaching agreement as the coaching team keeps it. */
data class DefaultCoachingAgreement(
    var code: String,
    var label: String,
    var ratio: Double,
    var createdAt: Int,
    var contactEmail: String,
    var maximum: Double,
    var name: String,
    var status: DefaultCoachingAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultCoachingAgreementStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultCoachingAgreementStatus): Boolean {
        when (status) {
            DefaultCoachingAgreementStatus.PLANNED -> {
                when (next) {
                    DefaultCoachingAgreementStatus.COMPLETED -> return true
                }
                return false
            }
            DefaultCoachingAgreementStatus.SCHEDULED -> {
                when (next) {
                    DefaultCoachingAgreementStatus.IN_PROGRESS, DefaultCoachingAgreementStatus.CANCELLED -> return true
                }
                return false
            }
            DefaultCoachingAgreementStatus.IN_PROGRESS -> {
                when (next) {
                    DefaultCoachingAgreementStatus.CANCELLED -> return true
                }
                return false
            }
            DefaultCoachingAgreementStatus.COMPLETED -> {
                when (next) {
                    DefaultCoachingAgreementStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultCoachingAgreementStatus) {
        if (!canMoveTo(next)) {
            error("default coaching agreement $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun effectiveTotal(): Double = round(createdAt.toDouble() * maximum * 100) / 100

    fun snapshot(): DefaultCoachingAgreement {
        return DefaultCoachingAgreement(
            code = code,
            label = label,
            ratio = ratio,
            createdAt = createdAt,
            contactEmail = contactEmail,
            maximum = maximum,
            name = name,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a default coaching agreement in status planned. */
fun newDefaultCoachingAgreement(code: String, label: String): DefaultCoachingAgreement {
    return DefaultCoachingAgreement(
        code = code,
        label = label,
        ratio = 0.0,
        createdAt = 0,
        contactEmail = "",
        maximum = 0.0,
        name = "",
        status = DefaultCoachingAgreementStatus.PLANNED,
    )
}
