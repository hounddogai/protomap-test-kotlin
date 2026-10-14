package com.acme.transport.safety.coaching.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a coaching summary is in its life cycle. */
enum class CoachingSummaryStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One coaching summary, with what the planners need to act on it. */
data class CoachingSummary(
    var code: String,
    var name: String,
    var pinned: Boolean,
    var tags: MutableList<String>,
    var rate: Double,
    var loadKg: Double,
    var active: Boolean,
    var dock: String,
    var depot: String,
    var ceiling: Double,
    var status: CoachingSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CoachingSummaryStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: CoachingSummaryStatus): Boolean {
        when (status) {
            CoachingSummaryStatus.NEW -> {
                when (next) {
                    CoachingSummaryStatus.QUEUED, CoachingSummaryStatus.DONE -> return true
                }
                return false
            }
            CoachingSummaryStatus.QUEUED -> {
                when (next) {
                    CoachingSummaryStatus.RUNNING, CoachingSummaryStatus.FAILED -> return true
                }
                return false
            }
            CoachingSummaryStatus.RUNNING -> {
                when (next) {
                    CoachingSummaryStatus.FAILED -> return true
                }
                return false
            }
            CoachingSummaryStatus.DONE -> {
                when (next) {
                    CoachingSummaryStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CoachingSummaryStatus) {
        if (!canMoveTo(next)) {
            error("coaching summary $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedLoad(): Double = round(rate * ceiling * 100) / 100

    fun snapshot(): CoachingSummary {
        return CoachingSummary(
            code = code,
            name = name,
            pinned = pinned,
            tags = tags,
            rate = rate,
            loadKg = loadKg,
            active = active,
            dock = dock,
            depot = depot,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a coaching summary in status new. */
fun newCoachingSummary(code: String, name: String): CoachingSummary {
    return CoachingSummary(
        code = code,
        name = name,
        pinned = false,
        tags = mutableListOf<String>(),
        rate = 0.0,
        loadKg = 0.0,
        active = false,
        dock = "",
        depot = "",
        ceiling = 0.0,
        status = CoachingSummaryStatus.NEW,
    )
}
