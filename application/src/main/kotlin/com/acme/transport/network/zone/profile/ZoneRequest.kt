package com.acme.transport.network.zone.profile

import com.acme.transport.kit.text.normalize

/** Where a zone request is in its life cycle. */
enum class ZoneRequestStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a zone request and its current status. */
data class ZoneRequest(
    var code: String,
    var name: String,
    var attributes: MutableMap<String, String>,
    var active: Boolean,
    var floorValue: Double,
    var archived: Boolean,
    var externalRef: String,
    var status: ZoneRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ZoneRequestStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ZoneRequestStatus): Boolean {
        when (status) {
            ZoneRequestStatus.NEW -> return next == ZoneRequestStatus.RUNNING || next == ZoneRequestStatus.DONE
            ZoneRequestStatus.QUEUED -> return next == ZoneRequestStatus.RUNNING || next == ZoneRequestStatus.DONE
            ZoneRequestStatus.RUNNING -> {
                when (next) {
                    ZoneRequestStatus.DONE -> return true
                }
                return false
            }
            ZoneRequestStatus.DONE -> return next == ZoneRequestStatus.FAILED
        }
        return false
    }

    fun moveTo(next: ZoneRequestStatus) {
        if (!canMoveTo(next)) {
            error("zone request $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = floorValue

    fun snapshot(): ZoneRequest {
        return ZoneRequest(
            code = code,
            name = name,
            attributes = attributes,
            active = active,
            floorValue = floorValue,
            archived = archived,
            externalRef = externalRef,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a zone request in status new. */
fun newZoneRequest(code: String, name: String): ZoneRequest {
    return ZoneRequest(
        code = code,
        name = name,
        attributes = mutableMapOf<String, String>(),
        active = false,
        floorValue = 0.0,
        archived = false,
        externalRef = "",
        status = ZoneRequestStatus.NEW,
    )
}
