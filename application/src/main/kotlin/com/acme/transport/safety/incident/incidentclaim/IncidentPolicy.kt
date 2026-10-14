package com.acme.transport.safety.incident.incidentclaim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a incident policy is in its life cycle. */
enum class IncidentPolicyStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a incident policy and its current status. */
data class IncidentPolicy(
    var code: String,
    var name: String,
    var timezone: String,
    var minimum: Double,
    var lane: String,
    var startsAt: Int,
    var externalRef: String,
    var loadKg: Double,
    var dock: String,
    var depot: String,
    var region: String,
    var status: IncidentPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IncidentPolicyStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: IncidentPolicyStatus): Boolean {
        when (status) {
            IncidentPolicyStatus.NEW -> return next == IncidentPolicyStatus.QUEUED || next == IncidentPolicyStatus.RUNNING
            IncidentPolicyStatus.QUEUED -> {
                when (next) {
                    IncidentPolicyStatus.FAILED -> return true
                }
                return false
            }
            IncidentPolicyStatus.RUNNING -> return next == IncidentPolicyStatus.FAILED
            IncidentPolicyStatus.DONE -> return next == IncidentPolicyStatus.FAILED
        }
        return false
    }

    fun moveTo(next: IncidentPolicyStatus) {
        if (!canMoveTo(next)) {
            error("incident policy $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun effectiveValue(): Double = round((loadKg + startsAt.toDouble()) * 100) / 100

    fun snapshot(): IncidentPolicy {
        return IncidentPolicy(
            code = code,
            name = name,
            timezone = timezone,
            minimum = minimum,
            lane = lane,
            startsAt = startsAt,
            externalRef = externalRef,
            loadKg = loadKg,
            dock = dock,
            depot = depot,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a incident policy in status new. */
fun newIncidentPolicy(code: String, name: String): IncidentPolicy {
    return IncidentPolicy(
        code = code,
        name = name,
        timezone = "",
        minimum = 0.0,
        lane = "",
        startsAt = 0,
        externalRef = "",
        loadKg = 0.0,
        dock = "",
        depot = "",
        region = "",
        status = IncidentPolicyStatus.NEW,
    )
}
