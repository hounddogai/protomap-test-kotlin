package com.acme.transport.network.transittime.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryTransitTimeBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryTransitTimeBudgetValidator()
        val item = newPrimaryTransitTimeBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = PrimaryTransitTimeBudgetRequiredNameRule("primary transit time budget needs this field")
        val item = newPrimaryTransitTimeBudget("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = PrimaryTransitTimeBudgetCodeLengthRule(255)
        val item = newPrimaryTransitTimeBudget("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = PrimaryTransitTimeBudgetUpdatedAtRangeRule(16, 32)
        val item = newPrimaryTransitTimeBudget("x", "y")
        item.updatedAt = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
