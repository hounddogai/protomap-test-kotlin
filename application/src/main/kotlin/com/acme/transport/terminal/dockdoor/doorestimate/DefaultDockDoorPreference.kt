package com.acme.transport.terminal.dockdoor.doorestimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default dock door preference is in its life cycle. */
enum class DefaultDockDoorPreferenceStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a default dock door preference and its current status. */
data class DefaultDockDoorPreference(
    var code: String,
    var name: String,
    var region: String,
    var batchSize: Int,
    var timezone: String,
    var routeCode: String,
    var active: Boolean,
    var lane: String,
    var dock: String,
    var externalRef: String,
    var score: Double,
    var status: DefaultDockDoorPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultDockDoorPreferenceStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultDockDoorPreferenceStatus): Boolean {
        when (status) {
            DefaultDockDoorPreferenceStatus.IDLE -> {
                when (next) {
                    DefaultDockDoorPreferenceStatus.WARMING, DefaultDockDoorPreferenceStatus.STOPPED -> return true
                }
                return false
            }
            DefaultDockDoorPreferenceStatus.WARMING -> {
                when (next) {
                    DefaultDockDoorPreferenceStatus.READY, DefaultDockDoorPreferenceStatus.DRAINING -> return true
                }
                return false
            }
            DefaultDockDoorPreferenceStatus.READY -> {
                when (next) {
                    DefaultDockDoorPreferenceStatus.DRAINING -> return true
                }
                return false
            }
            DefaultDockDoorPreferenceStatus.DRAINING -> {
                when (next) {
                    DefaultDockDoorPreferenceStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultDockDoorPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("default dock door preference $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun effectiveValue(): Double = round((score - batchSize.toDouble()) * 100) / 100

    fun snapshot(): DefaultDockDoorPreference {
        return DefaultDockDoorPreference(
            code = code,
            name = name,
            region = region,
            batchSize = batchSize,
            timezone = timezone,
            routeCode = routeCode,
            active = active,
            lane = lane,
            dock = dock,
            externalRef = externalRef,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a default dock door preference in status idle. */
fun newDefaultDockDoorPreference(code: String, name: String): DefaultDockDoorPreference {
    return DefaultDockDoorPreference(
        code = code,
        name = name,
        region = "",
        batchSize = 0,
        timezone = "",
        routeCode = "",
        active = false,
        lane = "",
        dock = "",
        externalRef = "",
        score = 0.0,
        status = DefaultDockDoorPreferenceStatus.IDLE,
    )
}
