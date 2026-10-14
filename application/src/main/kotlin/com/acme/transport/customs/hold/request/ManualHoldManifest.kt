package com.acme.transport.customs.hold.request

import com.acme.transport.kit.text.normalize

/** Where a manual hold manifest is in its life cycle. */
enum class ManualHoldManifestStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A manual hold manifest. Its code is unique within a tenant. */
data class ManualHoldManifest(
    var code: String,
    var name: String,
    var etaMinutes: Int,
    var title: String,
    var notes: String,
    var routeCode: String,
    var status: ManualHoldManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualHoldManifestStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualHoldManifestStatus): Boolean {
        when (status) {
            ManualHoldManifestStatus.DRAFT -> {
                when (next) {
                    ManualHoldManifestStatus.PENDING, ManualHoldManifestStatus.CLOSED -> return true
                }
                return false
            }
            ManualHoldManifestStatus.PENDING -> return next == ManualHoldManifestStatus.CLOSED
            ManualHoldManifestStatus.ACTIVE -> return next == ManualHoldManifestStatus.SUSPENDED
            ManualHoldManifestStatus.SUSPENDED -> {
                when (next) {
                    ManualHoldManifestStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualHoldManifestStatus) {
        if (!canMoveTo(next)) {
            error("manual hold manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun measuredValue(): Double = etaMinutes.toDouble()

    fun snapshot(): ManualHoldManifest {
        return ManualHoldManifest(
            code = code,
            name = name,
            etaMinutes = etaMinutes,
            title = title,
            notes = notes,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a manual hold manifest in status draft. */
fun newManualHoldManifest(code: String, name: String): ManualHoldManifest {
    return ManualHoldManifest(
        code = code,
        name = name,
        etaMinutes = 0,
        title = "",
        notes = "",
        routeCode = "",
        status = ManualHoldManifestStatus.DRAFT,
    )
}
