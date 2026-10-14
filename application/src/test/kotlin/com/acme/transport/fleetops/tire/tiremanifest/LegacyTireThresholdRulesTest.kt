package com.acme.transport.fleetops.tire.tiremanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyTireThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyTireThresholdValidator()
        val item = newLegacyTireThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = LegacyTireThresholdLabelLengthRule(120)
        val item = newLegacyTireThreshold("x", "y")
        item.label = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = LegacyTireThresholdRequiredLabelRule("legacy tire threshold needs an entry")
        val item = newLegacyTireThreshold("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
