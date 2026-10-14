package com.acme.transport.network.servicearea.cappedareaassignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default service area milestone is in its life cycle. */
enum class DefaultServiceAreaMilestoneStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One default service area milestone, with what the planners need to act on it. */
data class DefaultServiceAreaMilestone(
    var code: String,
    var title: String,
    var label: String,
    var sequence: Int,
    var graceDays: Int,
    var dock: String,
    var segment: String,
    var threshold: Double,
    var phone: String,
    var trailerId: String,
    var timezone: String,
    var status: DefaultServiceAreaMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultServiceAreaMilestoneStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultServiceAreaMilestoneStatus): Boolean {
        when (status) {
            DefaultServiceAreaMilestoneStatus.NEW -> return next == DefaultServiceAreaMilestoneStatus.QUEUED || next == DefaultServiceAreaMilestoneStatus.DONE
            DefaultServiceAreaMilestoneStatus.QUEUED -> {
                when (next) {
                    DefaultServiceAreaMilestoneStatus.RUNNING, DefaultServiceAreaMilestoneStatus.FAILED -> return true
                }
                return false
            }
            DefaultServiceAreaMilestoneStatus.RUNNING -> {
                when (next) {
                    DefaultServiceAreaMilestoneStatus.FAILED -> return true
                }
                return false
            }
            DefaultServiceAreaMilestoneStatus.DONE -> {
                when (next) {
                    DefaultServiceAreaMilestoneStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultServiceAreaMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("default service area milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$title]"

    fun weightedValue(): Double = round((sequence.toDouble() - graceDays.toDouble()) * 100) / 100

    fun snapshot(): DefaultServiceAreaMilestone {
        return DefaultServiceAreaMilestone(
            code = code,
            title = title,
            label = label,
            sequence = sequence,
            graceDays = graceDays,
            dock = dock,
            segment = segment,
            threshold = threshold,
            phone = phone,
            trailerId = trailerId,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a default service area milestone in status new. */
fun newDefaultServiceAreaMilestone(code: String, title: String): DefaultServiceAreaMilestone {
    return DefaultServiceAreaMilestone(
        code = code,
        title = title,
        label = "",
        sequence = 0,
        graceDays = 0,
        dock = "",
        segment = "",
        threshold = 0.0,
        phone = "",
        trailerId = "",
        timezone = "",
        status = DefaultServiceAreaMilestoneStatus.NEW,
    )
}
