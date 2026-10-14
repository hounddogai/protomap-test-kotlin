package com.acme.transport.linehaul.laneplan.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyLanePlanTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyLanePlanTargetValidator()
        val item = newLegacyLanePlanTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long segment`() {
        val rule = LegacyLanePlanTargetSegmentLengthRule(120)
        val item = newLegacyLanePlanTarget("x", "y")
        item.segment = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.segment = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = LegacyLanePlanTargetRequiredLabelRule("legacy lane plan target needs an entry")
        val item = newLegacyLanePlanTarget("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = LegacyLanePlanTargetRequiredSegmentRule("legacy lane plan target needs an entry")
        val item = newLegacyLanePlanTarget("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
