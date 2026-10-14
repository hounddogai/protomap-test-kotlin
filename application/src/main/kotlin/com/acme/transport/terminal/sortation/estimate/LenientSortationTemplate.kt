package com.acme.transport.terminal.sortation.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient sortation template is in its life cycle. */
enum class LenientSortationTemplateStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A lenient sortation template as the sortation team keeps it. */
data class LenientSortationTemplate(
    var code: String,
    var label: String,
    var sequence: Int,
    var phone: String,
    var tags: MutableList<String>,
    var loadKg: Double,
    var status: LenientSortationTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientSortationTemplateStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientSortationTemplateStatus): Boolean {
        when (status) {
            LenientSortationTemplateStatus.NEW -> return next == LenientSortationTemplateStatus.QUEUED || next == LenientSortationTemplateStatus.RUNNING
            LenientSortationTemplateStatus.QUEUED -> return next == LenientSortationTemplateStatus.DONE || next == LenientSortationTemplateStatus.FAILED
            LenientSortationTemplateStatus.RUNNING -> {
                when (next) {
                    LenientSortationTemplateStatus.DONE, LenientSortationTemplateStatus.FAILED -> return true
                }
                return false
            }
            LenientSortationTemplateStatus.DONE -> {
                when (next) {
                    LenientSortationTemplateStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientSortationTemplateStatus) {
        if (!canMoveTo(next)) {
            error("lenient sortation template $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netValue(): Double = round((sequence.toDouble() + loadKg) * 100) / 100

    fun snapshot(): LenientSortationTemplate {
        return LenientSortationTemplate(
            code = code,
            label = label,
            sequence = sequence,
            phone = phone,
            tags = tags,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a lenient sortation template in status new. */
fun newLenientSortationTemplate(code: String, label: String): LenientSortationTemplate {
    return LenientSortationTemplate(
        code = code,
        label = label,
        sequence = 0,
        phone = "",
        tags = mutableListOf<String>(),
        loadKg = 0.0,
        status = LenientSortationTemplateStatus.NEW,
    )
}
