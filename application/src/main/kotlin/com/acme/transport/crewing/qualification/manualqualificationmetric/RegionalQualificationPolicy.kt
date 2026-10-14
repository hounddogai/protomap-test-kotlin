package com.acme.transport.crewing.qualification.manualqualificationmetric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional qualification policy is in its life cycle. */
enum class RegionalQualificationPolicyStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One regional qualification policy, with what the planners need to act on it. */
data class RegionalQualificationPolicy(
    var code: String,
    var name: String,
    var endsAt: Int,
    var routeCode: String,
    var minimum: Double,
    var approved: Boolean,
    var description: String,
    var distanceKm: Double,
    var status: RegionalQualificationPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalQualificationPolicyStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalQualificationPolicyStatus): Boolean {
        when (status) {
            RegionalQualificationPolicyStatus.NEW -> {
                when (next) {
                    RegionalQualificationPolicyStatus.QUEUED -> return true
                }
                return false
            }
            RegionalQualificationPolicyStatus.QUEUED -> return next == RegionalQualificationPolicyStatus.DONE
            RegionalQualificationPolicyStatus.RUNNING -> {
                when (next) {
                    RegionalQualificationPolicyStatus.DONE, RegionalQualificationPolicyStatus.FAILED -> return true
                }
                return false
            }
            RegionalQualificationPolicyStatus.DONE -> return next == RegionalQualificationPolicyStatus.FAILED
        }
        return false
    }

    fun moveTo(next: RegionalQualificationPolicyStatus) {
        if (!canMoveTo(next)) {
            error("regional qualification policy $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun weightedExposure(): Double = round(endsAt.toDouble() * distanceKm * 100) / 100

    fun snapshot(): RegionalQualificationPolicy {
        return RegionalQualificationPolicy(
            code = code,
            name = name,
            endsAt = endsAt,
            routeCode = routeCode,
            minimum = minimum,
            approved = approved,
            description = description,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a regional qualification policy in status new. */
fun newRegionalQualificationPolicy(code: String, name: String): RegionalQualificationPolicy {
    return RegionalQualificationPolicy(
        code = code,
        name = name,
        endsAt = 0,
        routeCode = "",
        minimum = 0.0,
        approved = false,
        description = "",
        distanceKm = 0.0,
        status = RegionalQualificationPolicyStatus.NEW,
    )
}
