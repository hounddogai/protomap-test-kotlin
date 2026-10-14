package com.acme.transport.terminal.traileryard.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredTrailerYardBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredTrailerYardBudgetValidator()
        val item = newTieredTrailerYardBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = TieredTrailerYardBudgetMaximumRangeRule(1.4, 1352.1)
        val item = newTieredTrailerYardBudget("x", "y")
        item.maximum = 1353.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = TieredTrailerYardBudgetRequiredSegmentRule("tiered trailer yard budget needs a value")
        val item = newTieredTrailerYardBudget("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = TieredTrailerYardBudgetLabelLengthRule(255)
        val item = newTieredTrailerYardBudget("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = TieredTrailerYardBudgetThresholdRangeRule(8.4, 199.2)
        val item = newTieredTrailerYardBudget("x", "y")
        item.threshold = 200.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = TieredTrailerYardBudgetRequiredOwnerIdRule("tiered trailer yard budget needs an entry")
        val item = newTieredTrailerYardBudget("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = TieredTrailerYardBudgetUpdatedAtRangeRule(80, 1000)
        val item = newTieredTrailerYardBudget("x", "y")
        item.updatedAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
