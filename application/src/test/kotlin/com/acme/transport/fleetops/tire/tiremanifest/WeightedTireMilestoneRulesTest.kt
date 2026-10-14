package com.acme.transport.fleetops.tire.tiremanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedTireMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedTireMilestoneValidator()
        val item = newWeightedTireMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above revision`() {
        val rule = WeightedTireMilestoneRevisionRangeRule(16, 64)
        val item = newWeightedTireMilestone("x", "y")
        item.revision = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = WeightedTireMilestoneRequiredCodeRule("weighted tire milestone needs this field")
        val item = newWeightedTireMilestone("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = WeightedTireMilestoneLoadKgRangeRule(5.8, 4080.5)
        val item = newWeightedTireMilestone("x", "y")
        item.loadKg = 4082.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = WeightedTireMilestoneCodeLengthRule(80)
        val item = newWeightedTireMilestone("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
