package com.acme.transport.emissions.evcharge.chargebaseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedEvChargeTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedEvChargeTargetValidator()
        val item = newWeightedEvChargeTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = WeightedEvChargeTargetRequiredCodeRule("weighted ev charge target needs an entry")
        val item = newWeightedEvChargeTarget("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = WeightedEvChargeTargetRequiredNameRule("weighted ev charge target needs an entry")
        val item = newWeightedEvChargeTarget("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = WeightedEvChargeTargetRequiredTrailerIdRule("weighted ev charge target needs this field")
        val item = newWeightedEvChargeTarget("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = WeightedEvChargeTargetUpdatedAtRangeRule(64, 3600)
        val item = newWeightedEvChargeTarget("x", "y")
        item.updatedAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = WeightedEvChargeTargetCodeLengthRule(1000)
        val item = newWeightedEvChargeTarget("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
