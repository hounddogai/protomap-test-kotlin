package com.acme.transport.terminal.sortation.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive sortation bundle is in its life cycle. */
enum class AdaptiveSortationBundleStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A adaptive sortation bundle as the sortation team keeps it. */
data class AdaptiveSortationBundle(
    var code: String,
    var label: String,
    var graceDays: Int,
    var channel: String,
    var externalRef: String,
    var sequence: Int,
    var status: AdaptiveSortationBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveSortationBundleStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveSortationBundleStatus): Boolean {
        when (status) {
            AdaptiveSortationBundleStatus.REQUESTED -> {
                when (next) {
                    AdaptiveSortationBundleStatus.GRANTED -> return true
                }
                return false
            }
            AdaptiveSortationBundleStatus.GRANTED -> {
                when (next) {
                    AdaptiveSortationBundleStatus.REVOKED, AdaptiveSortationBundleStatus.EXPIRED -> return true
                }
                return false
            }
            AdaptiveSortationBundleStatus.REVOKED -> {
                when (next) {
                    AdaptiveSortationBundleStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveSortationBundleStatus) {
        if (!canMoveTo(next)) {
            error("adaptive sortation bundle $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun adjustedLoad(): Double = round((graceDays.toDouble() + sequence.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveSortationBundle {
        return AdaptiveSortationBundle(
            code = code,
            label = label,
            graceDays = graceDays,
            channel = channel,
            externalRef = externalRef,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a adaptive sortation bundle in status requested. */
fun newAdaptiveSortationBundle(code: String, label: String): AdaptiveSortationBundle {
    return AdaptiveSortationBundle(
        code = code,
        label = label,
        graceDays = 0,
        channel = "",
        externalRef = "",
        sequence = 0,
        status = AdaptiveSortationBundleStatus.REQUESTED,
    )
}
