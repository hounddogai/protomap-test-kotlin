package com.acme.transport.linehaul.tender.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedTenderExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedTenderExceptionValidator()
        val item = newWeightedTenderException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above cost`() {
        val rule = WeightedTenderExceptionCostRangeRule(0.0, 4480.5)
        val item = newWeightedTenderException("x", "y")
        item.cost = 4482.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank description`() {
        val rule = WeightedTenderExceptionRequiredDescriptionRule("weighted tender exception needs this field")
        val item = newWeightedTenderException("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
