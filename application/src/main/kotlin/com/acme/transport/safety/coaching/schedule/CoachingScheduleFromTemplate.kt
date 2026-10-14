package com.acme.transport.safety.coaching.schedule

import com.acme.transport.safety.scorecard.notice.ScorecardTemplate
import com.acme.transport.safety.scorecard.notice.scorecardNoticeBucket

fun summarizeScorecardTemplatesForSchedule(items: MutableList<ScorecardTemplate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveCoachingSchedulesFromTemplate(items: MutableList<ScorecardTemplate>): MutableList<CoachingSchedule> {
    val out = mutableListOf<CoachingSchedule>()
    for (item in items) {
        val derived = newCoachingSchedule(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun mergeCoachingSchedule(item: CoachingSchedule): String = scorecardNoticeBucket(item.adjustedExposure())
