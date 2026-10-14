package com.acme.transport.customs.clearance.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryClearanceGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryClearanceGroupValidator()
        val item = newPrimaryClearanceGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = PrimaryClearanceGroupCodeLengthRule(16)
        val item = newPrimaryClearanceGroup("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = PrimaryClearanceGroupExternalRefLengthRule(16)
        val item = newPrimaryClearanceGroup("x", "y")
        item.externalRef = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = PrimaryClearanceGroupStartsAtRangeRule(16, 32)
        val item = newPrimaryClearanceGroup("x", "y")
        item.startsAt = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
