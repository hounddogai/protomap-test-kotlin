package com.acme.transport.crewing.swap.swapnotice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a swap profile is in its life cycle. */
enum class SwapProfileStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One swap profile, with what the planners need to act on it. */
data class SwapProfile(
    var code: String,
    var name: String,
    var pallets: Int,
    var label: String,
    var budget: Double,
    var pinned: Boolean,
    var status: SwapProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SwapProfileStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: SwapProfileStatus): Boolean {
        when (status) {
            SwapProfileStatus.NEW -> {
                when (next) {
                    SwapProfileStatus.FAILED -> return true
                }
                return false
            }
            SwapProfileStatus.QUEUED -> return next == SwapProfileStatus.RUNNING || next == SwapProfileStatus.DONE
            SwapProfileStatus.RUNNING -> return next == SwapProfileStatus.FAILED
            SwapProfileStatus.DONE -> {
                when (next) {
                    SwapProfileStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SwapProfileStatus) {
        if (!canMoveTo(next)) {
            error("swap profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$name]"

    fun weightedTotal(): Double = round((budget - pallets.toDouble()) * 100) / 100

    fun snapshot(): SwapProfile {
        return SwapProfile(
            code = code,
            name = name,
            pallets = pallets,
            label = label,
            budget = budget,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a swap profile in status new. */
fun newSwapProfile(code: String, name: String): SwapProfile {
    return SwapProfile(
        code = code,
        name = name,
        pallets = 0,
        label = "",
        budget = 0.0,
        pinned = false,
        status = SwapProfileStatus.NEW,
    )
}
