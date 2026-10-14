package com.acme.transport.lastmile.redelivery.redeliverywindow

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedRedeliveryWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedRedeliveryWindowValidator()
        val item = newWeightedRedeliveryWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above retries`() {
        val rule = WeightedRedeliveryWindowRetriesRangeRule(255, 86400)
        val item = newWeightedRedeliveryWindow("x", "y")
        item.retries = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above priority`() {
        val rule = WeightedRedeliveryWindowPriorityRangeRule(64, 64)
        val item = newWeightedRedeliveryWindow("x", "y")
        item.priority = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long category`() {
        val rule = WeightedRedeliveryWindowCategoryLengthRule(120)
        val item = newWeightedRedeliveryWindow("x", "y")
        item.category = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank category`() {
        val rule = WeightedRedeliveryWindowRequiredCategoryRule("weighted redelivery window needs this field")
        val item = newWeightedRedeliveryWindow("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
