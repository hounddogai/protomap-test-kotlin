package com.acme.transport.safety.incident.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual incident manifest is in its life cycle. */
enum class ManualIncidentManifestStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A manual incident manifest. Its code is unique within a tenant. */
data class ManualIncidentManifest(
    var code: String,
    var name: String,
    var contactEmail: String,
    var maximum: Double,
    var weight: Double,
    var attempts: Int,
    var score: Double,
    var batchSize: Int,
    var summary: String,
    var status: ManualIncidentManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualIncidentManifestStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualIncidentManifestStatus): Boolean {
        when (status) {
            ManualIncidentManifestStatus.NEW -> return next == ManualIncidentManifestStatus.QUEUED
            ManualIncidentManifestStatus.QUEUED -> {
                when (next) {
                    ManualIncidentManifestStatus.FAILED -> return true
                }
                return false
            }
            ManualIncidentManifestStatus.RUNNING -> return next == ManualIncidentManifestStatus.DONE || next == ManualIncidentManifestStatus.FAILED
            ManualIncidentManifestStatus.DONE -> {
                when (next) {
                    ManualIncidentManifestStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualIncidentManifestStatus) {
        if (!canMoveTo(next)) {
            error("manual incident manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun effectiveTotal(): Double = round(weight * batchSize.toDouble() * 100) / 100

    fun snapshot(): ManualIncidentManifest {
        return ManualIncidentManifest(
            code = code,
            name = name,
            contactEmail = contactEmail,
            maximum = maximum,
            weight = weight,
            attempts = attempts,
            score = score,
            batchSize = batchSize,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a manual incident manifest in status new. */
fun newManualIncidentManifest(code: String, name: String): ManualIncidentManifest {
    return ManualIncidentManifest(
        code = code,
        name = name,
        contactEmail = "",
        maximum = 0.0,
        weight = 0.0,
        attempts = 0,
        score = 0.0,
        batchSize = 0,
        summary = "",
        status = ManualIncidentManifestStatus.NEW,
    )
}
