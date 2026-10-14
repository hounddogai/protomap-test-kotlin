package com.acme.transport.emissions.emissionfactor.digest

import com.acme.transport.kit.text.normalize

/** Where a weighted emission factor group is in its life cycle. */
enum class WeightedEmissionFactorGroupStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A weighted emission factor group. Its code is unique within a tenant. */
data class WeightedEmissionFactorGroup(
    var code: String,
    var name: String,
    var locale: String,
    var phone: String,
    var dock: String,
    var segment: String,
    var limit: Int,
    var status: WeightedEmissionFactorGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedEmissionFactorGroupStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedEmissionFactorGroupStatus): Boolean {
        when (status) {
            WeightedEmissionFactorGroupStatus.REQUESTED -> return next == WeightedEmissionFactorGroupStatus.GRANTED
            WeightedEmissionFactorGroupStatus.GRANTED -> return next == WeightedEmissionFactorGroupStatus.REVOKED
            WeightedEmissionFactorGroupStatus.REVOKED -> return next == WeightedEmissionFactorGroupStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: WeightedEmissionFactorGroupStatus) {
        if (!canMoveTo(next)) {
            error("weighted emission factor group $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun measuredValue(): Double = limit.toDouble()

    fun snapshot(): WeightedEmissionFactorGroup {
        return WeightedEmissionFactorGroup(
            code = code,
            name = name,
            locale = locale,
            phone = phone,
            dock = dock,
            segment = segment,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a weighted emission factor group in status requested. */
fun newWeightedEmissionFactorGroup(code: String, name: String): WeightedEmissionFactorGroup {
    return WeightedEmissionFactorGroup(
        code = code,
        name = name,
        locale = "",
        phone = "",
        dock = "",
        segment = "",
        limit = 0,
        status = WeightedEmissionFactorGroupStatus.REQUESTED,
    )
}
