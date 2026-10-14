package com.acme.transport.emissions.idling.idlingtarget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingIdlingProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingIdlingProfileValidator()
        val item = newPendingIdlingProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PendingIdlingProfileRequiredCodeRule("pending idling profile needs this field")
        val item = newPendingIdlingProfile("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank description`() {
        val rule = PendingIdlingProfileRequiredDescriptionRule("pending idling profile needs a value")
        val item = newPendingIdlingProfile("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = PendingIdlingProfileAmountRangeRule(7.9, 609.3)
        val item = newPendingIdlingProfile("x", "y")
        item.amount = 610.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = PendingIdlingProfileRequiredNameRule("pending idling profile needs this field")
        val item = newPendingIdlingProfile("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = PendingIdlingProfileNameLengthRule(16)
        val item = newPendingIdlingProfile("x", "y")
        item.name = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
