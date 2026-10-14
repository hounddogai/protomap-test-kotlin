package com.acme.transport.linehaul.laneplan.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched lane plan bundle is in its life cycle. */
enum class BatchedLanePlanBundleStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A batched lane plan bundle as the lane plan team keeps it. */
data class BatchedLanePlanBundle(
    var code: String,
    var title: String,
    var tags: MutableList<String>,
    var retries: Int,
    var summary: String,
    var windowDays: Int,
    var rate: Double,
    var revision: Int,
    var status: BatchedLanePlanBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedLanePlanBundleStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedLanePlanBundleStatus): Boolean {
        when (status) {
            BatchedLanePlanBundleStatus.NEW -> return next == BatchedLanePlanBundleStatus.RUNNING
            BatchedLanePlanBundleStatus.QUEUED -> {
                when (next) {
                    BatchedLanePlanBundleStatus.RUNNING, BatchedLanePlanBundleStatus.DONE -> return true
                }
                return false
            }
            BatchedLanePlanBundleStatus.RUNNING -> return next == BatchedLanePlanBundleStatus.DONE || next == BatchedLanePlanBundleStatus.FAILED
            BatchedLanePlanBundleStatus.DONE -> {
                when (next) {
                    BatchedLanePlanBundleStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedLanePlanBundleStatus) {
        if (!canMoveTo(next)) {
            error("batched lane plan bundle $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"batched lane plan bundle"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedExposure(): Double = round((rate - windowDays.toDouble()) * 100) / 100

    fun snapshot(): BatchedLanePlanBundle {
        return BatchedLanePlanBundle(
            code = code,
            title = title,
            tags = tags,
            retries = retries,
            summary = summary,
            windowDays = windowDays,
            rate = rate,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a batched lane plan bundle in status new. */
fun newBatchedLanePlanBundle(code: String, title: String): BatchedLanePlanBundle {
    return BatchedLanePlanBundle(
        code = code,
        title = title,
        tags = mutableListOf<String>(),
        retries = 0,
        summary = "",
        windowDays = 0,
        rate = 0.0,
        revision = 0,
        status = BatchedLanePlanBundleStatus.NEW,
    )
}
