package com.acme.transport.linehaul.tender.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tender manifest is in its life cycle. */
enum class TenderManifestStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a tender manifest and its current status. */
data class TenderManifest(
    var code: String,
    var name: String,
    var rate: Double,
    var label: String,
    var maximum: Double,
    var revision: Int,
    var status: TenderManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TenderManifestStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: TenderManifestStatus): Boolean {
        when (status) {
            TenderManifestStatus.NEW -> {
                when (next) {
                    TenderManifestStatus.RUNNING, TenderManifestStatus.FAILED -> return true
                }
                return false
            }
            TenderManifestStatus.QUEUED -> {
                when (next) {
                    TenderManifestStatus.RUNNING, TenderManifestStatus.FAILED -> return true
                }
                return false
            }
            TenderManifestStatus.RUNNING -> return next == TenderManifestStatus.DONE
            TenderManifestStatus.DONE -> {
                when (next) {
                    TenderManifestStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TenderManifestStatus) {
        if (!canMoveTo(next)) {
            error("tender manifest $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code [$name]"

    fun netValue(): Double = round(rate * revision.toDouble() * 100) / 100

    fun snapshot(): TenderManifest {
        return TenderManifest(
            code = code,
            name = name,
            rate = rate,
            label = label,
            maximum = maximum,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a tender manifest in status new. */
fun newTenderManifest(code: String, name: String): TenderManifest {
    return TenderManifest(
        code = code,
        name = name,
        rate = 0.0,
        label = "",
        maximum = 0.0,
        revision = 0,
        status = TenderManifestStatus.NEW,
    )
}
