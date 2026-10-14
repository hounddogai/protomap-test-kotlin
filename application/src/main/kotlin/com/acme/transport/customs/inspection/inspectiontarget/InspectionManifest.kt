package com.acme.transport.customs.inspection.inspectiontarget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a inspection manifest is in its life cycle. */
enum class InspectionManifestStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One inspection manifest, with what the planners need to act on it. */
data class InspectionManifest(
    var code: String,
    var name: String,
    var version: Int,
    var floorValue: Double,
    var maximum: Double,
    var dock: String,
    var segment: String,
    var revision: Int,
    var status: InspectionManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionManifestStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionManifestStatus): Boolean {
        when (status) {
            InspectionManifestStatus.NEW -> {
                when (next) {
                    InspectionManifestStatus.QUEUED, InspectionManifestStatus.RUNNING -> return true
                }
                return false
            }
            InspectionManifestStatus.QUEUED -> return next == InspectionManifestStatus.RUNNING || next == InspectionManifestStatus.FAILED
            InspectionManifestStatus.RUNNING -> {
                when (next) {
                    InspectionManifestStatus.DONE -> return true
                }
                return false
            }
            InspectionManifestStatus.DONE -> return next == InspectionManifestStatus.FAILED
        }
        return false
    }

    fun moveTo(next: InspectionManifestStatus) {
        if (!canMoveTo(next)) {
            error("inspection manifest $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "${"inspection manifest"} $code"

    fun weightedLoad(): Double = round(version.toDouble() * floorValue * 100) / 100

    fun snapshot(): InspectionManifest {
        return InspectionManifest(
            code = code,
            name = name,
            version = version,
            floorValue = floorValue,
            maximum = maximum,
            dock = dock,
            segment = segment,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a inspection manifest in status new. */
fun newInspectionManifest(code: String, name: String): InspectionManifest {
    return InspectionManifest(
        code = code,
        name = name,
        version = 0,
        floorValue = 0.0,
        maximum = 0.0,
        dock = "",
        segment = "",
        revision = 0,
        status = InspectionManifestStatus.NEW,
    )
}
