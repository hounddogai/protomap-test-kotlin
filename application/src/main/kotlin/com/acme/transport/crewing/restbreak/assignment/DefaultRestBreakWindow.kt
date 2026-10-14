package com.acme.transport.crewing.restbreak.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default rest break window is in its life cycle. */
enum class DefaultRestBreakWindowStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One default rest break window, with what the planners need to act on it. */
data class DefaultRestBreakWindow(
    var code: String,
    var title: String,
    var batchSize: Int,
    var description: String,
    var contactEmail: String,
    var updatedAt: Int,
    var status: DefaultRestBreakWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultRestBreakWindowStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultRestBreakWindowStatus): Boolean {
        when (status) {
            DefaultRestBreakWindowStatus.NEW -> return next == DefaultRestBreakWindowStatus.DONE
            DefaultRestBreakWindowStatus.QUEUED -> {
                when (next) {
                    DefaultRestBreakWindowStatus.FAILED -> return true
                }
                return false
            }
            DefaultRestBreakWindowStatus.RUNNING -> {
                when (next) {
                    DefaultRestBreakWindowStatus.DONE -> return true
                }
                return false
            }
            DefaultRestBreakWindowStatus.DONE -> {
                when (next) {
                    DefaultRestBreakWindowStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultRestBreakWindowStatus) {
        if (!canMoveTo(next)) {
            error("default rest break window $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun projectedTotal(): Double = round((updatedAt.toDouble() - batchSize.toDouble()) * 100) / 100

    fun snapshot(): DefaultRestBreakWindow {
        return DefaultRestBreakWindow(
            code = code,
            title = title,
            batchSize = batchSize,
            description = description,
            contactEmail = contactEmail,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a default rest break window in status new. */
fun newDefaultRestBreakWindow(code: String, title: String): DefaultRestBreakWindow {
    return DefaultRestBreakWindow(
        code = code,
        title = title,
        batchSize = 0,
        description = "",
        contactEmail = "",
        updatedAt = 0,
        status = DefaultRestBreakWindowStatus.NEW,
    )
}
