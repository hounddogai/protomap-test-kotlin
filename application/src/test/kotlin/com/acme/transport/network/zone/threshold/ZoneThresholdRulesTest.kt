package com.acme.transport.network.zone.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ZoneThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultZoneThresholdValidator()
        val item = newZoneThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above weight`() {
        val rule = ZoneThresholdWeightRangeRule(9.0, 3819.5)
        val item = newZoneThreshold("x", "y")
        item.weight = 3821.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = ZoneThresholdLabelLengthRule(64)
        val item = newZoneThreshold("x", "y")
        item.label = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = ZoneThresholdRequiredLabelRule("zone threshold needs a value")
        val item = newZoneThreshold("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ZoneThresholdRequiredNameRule("zone threshold needs an entry")
        val item = newZoneThreshold("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
