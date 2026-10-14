package com.acme.transport.network.cutoff.regionalcutoffsegment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a cutoff template is in its life cycle. */
enum class CutoffTemplateStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a cutoff template and its current status. */
data class CutoffTemplate(
    var code: String,
    var label: String,
    var endsAt: Int,
    var country: String,
    var rate: Double,
    var summary: String,
    var category: String,
    var lane: String,
    var contactEmail: String,
    var region: String,
    var status: CutoffTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CutoffTemplateStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: CutoffTemplateStatus): Boolean {
        when (status) {
            CutoffTemplateStatus.NEW -> {
                when (next) {
                    CutoffTemplateStatus.QUEUED, CutoffTemplateStatus.FAILED -> return true
                }
                return false
            }
            CutoffTemplateStatus.QUEUED -> return next == CutoffTemplateStatus.DONE
            CutoffTemplateStatus.RUNNING -> return next == CutoffTemplateStatus.FAILED
            CutoffTemplateStatus.DONE -> return next == CutoffTemplateStatus.FAILED
        }
        return false
    }

    fun moveTo(next: CutoffTemplateStatus) {
        if (!canMoveTo(next)) {
            error("cutoff template $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun weightedExposure(): Double = round(rate * endsAt.toDouble() * 100) / 100

    fun snapshot(): CutoffTemplate {
        return CutoffTemplate(
            code = code,
            label = label,
            endsAt = endsAt,
            country = country,
            rate = rate,
            summary = summary,
            category = category,
            lane = lane,
            contactEmail = contactEmail,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a cutoff template in status new. */
fun newCutoffTemplate(code: String, label: String): CutoffTemplate {
    return CutoffTemplate(
        code = code,
        label = label,
        endsAt = 0,
        country = "",
        rate = 0.0,
        summary = "",
        category = "",
        lane = "",
        contactEmail = "",
        region = "",
        status = CutoffTemplateStatus.NEW,
    )
}
