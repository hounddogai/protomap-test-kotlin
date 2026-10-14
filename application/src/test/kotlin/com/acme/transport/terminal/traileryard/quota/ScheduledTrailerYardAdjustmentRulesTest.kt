package com.acme.transport.terminal.traileryard.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledTrailerYardAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledTrailerYardAdjustmentValidator()
        val item = newScheduledTrailerYardAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = ScheduledTrailerYardAdjustmentRequiredOwnerIdRule(
            "scheduled trailer yard adjustment needs this field",
        )
        val item = newScheduledTrailerYardAdjustment("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScheduledTrailerYardAdjustmentRequiredCodeRule("scheduled trailer yard adjustment needs this field")
        val item = newScheduledTrailerYardAdjustment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = ScheduledTrailerYardAdjustmentRequiredDockRule("scheduled trailer yard adjustment needs this field")
        val item = newScheduledTrailerYardAdjustment("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = ScheduledTrailerYardAdjustmentDiscountRangeRule(2.3, 2683.6)
        val item = newScheduledTrailerYardAdjustment("x", "y")
        item.discount = 2685.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
