package com.acme.transport.emissions.emissionfactor.draft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a emission factor variance is in its life cycle. */
enum class EmissionFactorVarianceStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A emission factor variance. Its code is unique within a tenant. */
data class EmissionFactorVariance(
    var code: String,
    var name: String,
    var durationMinutes: Int,
    var tags: MutableList<String>,
    var depot: String,
    var discount: Double,
    var status: EmissionFactorVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EmissionFactorVarianceStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: EmissionFactorVarianceStatus): Boolean {
        when (status) {
            EmissionFactorVarianceStatus.NEW -> {
                when (next) {
                    EmissionFactorVarianceStatus.QUEUED, EmissionFactorVarianceStatus.DONE -> return true
                }
                return false
            }
            EmissionFactorVarianceStatus.QUEUED -> return next == EmissionFactorVarianceStatus.FAILED
            EmissionFactorVarianceStatus.RUNNING -> return next == EmissionFactorVarianceStatus.DONE || next == EmissionFactorVarianceStatus.FAILED
            EmissionFactorVarianceStatus.DONE -> return next == EmissionFactorVarianceStatus.FAILED
        }
        return false
    }

    fun moveTo(next: EmissionFactorVarianceStatus) {
        if (!canMoveTo(next)) {
            error("emission factor variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveLoad(): Double = round((durationMinutes.toDouble() - discount) * 100) / 100

    fun snapshot(): EmissionFactorVariance {
        return EmissionFactorVariance(
            code = code,
            name = name,
            durationMinutes = durationMinutes,
            tags = tags,
            depot = depot,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a emission factor variance in status new. */
fun newEmissionFactorVariance(code: String, name: String): EmissionFactorVariance {
    return EmissionFactorVariance(
        code = code,
        name = name,
        durationMinutes = 0,
        tags = mutableListOf<String>(),
        depot = "",
        discount = 0.0,
        status = EmissionFactorVarianceStatus.NEW,
    )
}
