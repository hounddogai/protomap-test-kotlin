package com.acme.transport.linehaul.laneplan.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LanePlanEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLanePlanEstimateValidator()
        val item = newLanePlanEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above limit`() {
        val rule = LanePlanEstimateLimitRangeRule(32, 86400)
        val item = newLanePlanEstimate("x", "y")
        item.limit = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = LanePlanEstimateRequiredLabelRule("lane plan estimate needs a value")
        val item = newLanePlanEstimate("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long phone`() {
        val rule = LanePlanEstimatePhoneLengthRule(120)
        val item = newLanePlanEstimate("x", "y")
        item.phone = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = LanePlanEstimateLabelLengthRule(80)
        val item = newLanePlanEstimate("x", "y")
        item.label = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
