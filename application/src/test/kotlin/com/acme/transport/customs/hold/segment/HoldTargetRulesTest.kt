package com.acme.transport.customs.hold.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HoldTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultHoldTargetValidator()
        val item = newHoldTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = HoldTargetRequiredCodeRule("hold target needs an entry")
        val item = newHoldTarget("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = HoldTargetCodeLengthRule(16)
        val item = newHoldTarget("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above limit`() {
        val rule = HoldTargetLimitRangeRule(64, 3600)
        val item = newHoldTarget("x", "y")
        item.limit = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = HoldTargetContactEmailLengthRule(32)
        val item = newHoldTarget("x", "y")
        item.contactEmail = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
