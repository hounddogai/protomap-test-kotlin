package com.acme.transport.lastmile.proofofdelivery.template

import com.acme.transport.kit.text.normalize

// The default proof of delivery template records of lastmile's proof of delivery work, and the rules, stores,
// and calculations around them.

/** Where a default proof of delivery template is in its life cycle. */
enum class DefaultProofOfDeliveryTemplateStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A default proof of delivery template. Its code is unique within a tenant. */
data class DefaultProofOfDeliveryTemplate(
    var code: String,
    var name: String,
    var routeCode: String,
    var unit: String,
    var minimum: Double,
    var status: DefaultProofOfDeliveryTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultProofOfDeliveryTemplateStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultProofOfDeliveryTemplateStatus): Boolean {
        when (status) {
            DefaultProofOfDeliveryTemplateStatus.OPEN -> {
                when (next) {
                    DefaultProofOfDeliveryTemplateStatus.IN_REVIEW -> return true
                }
                return false
            }
            DefaultProofOfDeliveryTemplateStatus.IN_REVIEW -> {
                when (next) {
                    DefaultProofOfDeliveryTemplateStatus.REOPENED, DefaultProofOfDeliveryTemplateStatus.ARCHIVED -> return true
                }
                return false
            }
            DefaultProofOfDeliveryTemplateStatus.RESOLVED -> {
                when (next) {
                    DefaultProofOfDeliveryTemplateStatus.REOPENED, DefaultProofOfDeliveryTemplateStatus.ARCHIVED -> return true
                }
                return false
            }
            DefaultProofOfDeliveryTemplateStatus.REOPENED -> {
                when (next) {
                    DefaultProofOfDeliveryTemplateStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultProofOfDeliveryTemplateStatus) {
        if (!canMoveTo(next)) {
            error("default proof of delivery template $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun measuredValue(): Double = minimum

    fun snapshot(): DefaultProofOfDeliveryTemplate {
        return DefaultProofOfDeliveryTemplate(
            code = code,
            name = name,
            routeCode = routeCode,
            unit = unit,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a default proof of delivery template in status open. */
fun newDefaultProofOfDeliveryTemplate(code: String, name: String): DefaultProofOfDeliveryTemplate {
    return DefaultProofOfDeliveryTemplate(
        code = code,
        name = name,
        routeCode = "",
        unit = "",
        minimum = 0.0,
        status = DefaultProofOfDeliveryTemplateStatus.OPEN,
    )
}
