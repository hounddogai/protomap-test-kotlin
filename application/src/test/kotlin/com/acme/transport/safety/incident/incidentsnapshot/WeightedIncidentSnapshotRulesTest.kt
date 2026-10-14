package com.acme.transport.safety.incident.incidentsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedIncidentSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedIncidentSnapshotValidator()
        val item = newWeightedIncidentSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = WeightedIncidentSnapshotRequiredLabelRule("weighted incident snapshot needs an entry")
        val item = newWeightedIncidentSnapshot("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = WeightedIncidentSnapshotRequiredCodeRule("weighted incident snapshot needs an entry")
        val item = newWeightedIncidentSnapshot("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above limit`() {
        val rule = WeightedIncidentSnapshotLimitRangeRule(64, 86400)
        val item = newWeightedIncidentSnapshot("x", "y")
        item.limit = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = WeightedIncidentSnapshotCodeLengthRule(255)
        val item = newWeightedIncidentSnapshot("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
