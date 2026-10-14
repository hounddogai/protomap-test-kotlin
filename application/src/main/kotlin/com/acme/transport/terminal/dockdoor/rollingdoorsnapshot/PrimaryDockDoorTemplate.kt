package com.acme.transport.terminal.dockdoor.rollingdoorsnapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary dock door template is in its life cycle. */
enum class PrimaryDockDoorTemplateStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A primary dock door template as the dock door team keeps it. */
data class PrimaryDockDoorTemplate(
    var code: String,
    var title: String,
    var name: String,
    var score: Double,
    var version: Int,
    var status: PrimaryDockDoorTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryDockDoorTemplateStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryDockDoorTemplateStatus): Boolean {
        when (status) {
            PrimaryDockDoorTemplateStatus.NEW -> return next == PrimaryDockDoorTemplateStatus.QUEUED
            PrimaryDockDoorTemplateStatus.QUEUED -> return next == PrimaryDockDoorTemplateStatus.DONE || next == PrimaryDockDoorTemplateStatus.FAILED
            PrimaryDockDoorTemplateStatus.RUNNING -> {
                when (next) {
                    PrimaryDockDoorTemplateStatus.FAILED -> return true
                }
                return false
            }
            PrimaryDockDoorTemplateStatus.DONE -> return next == PrimaryDockDoorTemplateStatus.FAILED
        }
        return false
    }

    fun moveTo(next: PrimaryDockDoorTemplateStatus) {
        if (!canMoveTo(next)) {
            error("primary dock door template $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code: $title"

    fun weightedValue(): Double = round((version.toDouble() + score) * 100) / 100

    fun snapshot(): PrimaryDockDoorTemplate {
        return PrimaryDockDoorTemplate(
            code = code,
            title = title,
            name = name,
            score = score,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a primary dock door template in status new. */
fun newPrimaryDockDoorTemplate(code: String, title: String): PrimaryDockDoorTemplate {
    return PrimaryDockDoorTemplate(
        code = code,
        title = title,
        name = "",
        score = 0.0,
        version = 0,
        status = PrimaryDockDoorTemplateStatus.NEW,
    )
}
