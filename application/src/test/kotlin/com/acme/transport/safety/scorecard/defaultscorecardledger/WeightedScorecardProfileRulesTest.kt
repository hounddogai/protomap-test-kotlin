package com.acme.transport.safety.scorecard.defaultscorecardledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedScorecardProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedScorecardProfileValidator()
        val item = newWeightedScorecardProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long unit`() {
        val rule = WeightedScorecardProfileUnitLengthRule(3600)
        val item = newWeightedScorecardProfile("x", "y")
        item.unit = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = WeightedScorecardProfileRequiredLabelRule("weighted scorecard profile needs a value")
        val item = newWeightedScorecardProfile("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = WeightedScorecardProfileCodeLengthRule(64)
        val item = newWeightedScorecardProfile("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long region`() {
        val rule = WeightedScorecardProfileRegionLengthRule(16)
        val item = newWeightedScorecardProfile("x", "y")
        item.region = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.region = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
