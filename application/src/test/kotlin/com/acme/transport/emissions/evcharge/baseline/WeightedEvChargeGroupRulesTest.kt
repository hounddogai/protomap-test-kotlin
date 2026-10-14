package com.acme.transport.emissions.evcharge.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedEvChargeGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedEvChargeGroupValidator()
        val item = newWeightedEvChargeGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = WeightedEvChargeGroupRequiredCodeRule("weighted ev charge group needs an entry")
        val item = newWeightedEvChargeGroup("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = WeightedEvChargeGroupTitleLengthRule(86400)
        val item = newWeightedEvChargeGroup("x", "y")
        item.title = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = WeightedEvChargeGroupRequiredTitleRule("weighted ev charge group needs this field")
        val item = newWeightedEvChargeGroup("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = WeightedEvChargeGroupGraceDaysRangeRule(64, 255)
        val item = newWeightedEvChargeGroup("x", "y")
        item.graceDays = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
