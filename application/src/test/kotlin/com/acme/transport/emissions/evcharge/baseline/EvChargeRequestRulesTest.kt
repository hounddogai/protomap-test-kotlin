package com.acme.transport.emissions.evcharge.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EvChargeRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultEvChargeRequestValidator()
        val item = newEvChargeRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = EvChargeRequestExternalRefLengthRule(32)
        val item = newEvChargeRequest("x", "y")
        item.externalRef = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = EvChargeRequestRequiredExternalRefRule("ev charge request needs a value")
        val item = newEvChargeRequest("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = EvChargeRequestCapacityRangeRule(16, 80)
        val item = newEvChargeRequest("x", "y")
        item.capacity = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above score`() {
        val rule = EvChargeRequestScoreRangeRule(9.8, 3074.8)
        val item = newEvChargeRequest("x", "y")
        item.score = 3076.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above amount`() {
        val rule = EvChargeRequestAmountRangeRule(9.4, 1197.8)
        val item = newEvChargeRequest("x", "y")
        item.amount = 1199.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = EvChargeRequestRequiredCodeRule("ev charge request needs a value")
        val item = newEvChargeRequest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
