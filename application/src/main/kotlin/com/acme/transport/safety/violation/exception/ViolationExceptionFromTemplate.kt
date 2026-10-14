package com.acme.transport.safety.violation.exception

import com.acme.transport.safety.cameraevent.score.CameraEventTemplate
import com.acme.transport.safety.cameraevent.score.normalizeCameraEventScoreCode

fun summarizeCameraEventTemplatesForException(items: MutableList<CameraEventTemplate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveViolationExceptionsFromTemplate(items: MutableList<CameraEventTemplate>): MutableList<ViolationException> {
    val out = mutableListOf<ViolationException>()
    for (item in items) {
        val derived = newViolationException(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun matchViolationException(item: ViolationException): String = normalizeCameraEventScoreCode(item.code)
