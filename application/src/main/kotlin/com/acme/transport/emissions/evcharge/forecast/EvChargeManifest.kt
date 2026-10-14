package com.acme.transport.emissions.evcharge.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a ev charge manifest is in its life cycle. */
enum class EvChargeManifestStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A ev charge manifest. Its code is unique within a tenant. */
data class EvChargeManifest(
    var code: String,
    var title: String,
    var segment: String,
    var margin: Double,
    var depot: String,
    var revision: Int,
    var locale: String,
    var status: EvChargeManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EvChargeManifestStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: EvChargeManifestStatus): Boolean {
        when (status) {
            EvChargeManifestStatus.NEW -> {
                when (next) {
                    EvChargeManifestStatus.QUEUED, EvChargeManifestStatus.FAILED -> return true
                }
                return false
            }
            EvChargeManifestStatus.QUEUED -> return next == EvChargeManifestStatus.RUNNING || next == EvChargeManifestStatus.DONE
            EvChargeManifestStatus.RUNNING -> {
                when (next) {
                    EvChargeManifestStatus.FAILED -> return true
                }
                return false
            }
            EvChargeManifestStatus.DONE -> return next == EvChargeManifestStatus.FAILED
        }
        return false
    }

    fun moveTo(next: EvChargeManifestStatus) {
        if (!canMoveTo(next)) {
            error("ev charge manifest $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code ($title)"

    fun netLoad(): Double = round(revision.toDouble() * margin * 100) / 100

    fun snapshot(): EvChargeManifest {
        return EvChargeManifest(
            code = code,
            title = title,
            segment = segment,
            margin = margin,
            depot = depot,
            revision = revision,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a ev charge manifest in status new. */
fun newEvChargeManifest(code: String, title: String): EvChargeManifest {
    return EvChargeManifest(
        code = code,
        title = title,
        segment = "",
        margin = 0.0,
        depot = "",
        revision = 0,
        locale = "",
        status = EvChargeManifestStatus.NEW,
    )
}
