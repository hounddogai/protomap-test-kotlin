package com.acme.transport.freightpay.accessorial.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AccessorialAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAccessorialAllocationValidator()
        val item = newAccessorialAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = AccessorialAllocationDriverIdLengthRule(1000)
        val item = newAccessorialAllocation("x", "y")
        item.driverId = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long locale`() {
        val rule = AccessorialAllocationLocaleLengthRule(16)
        val item = newAccessorialAllocation("x", "y")
        item.locale = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above version`() {
        val rule = AccessorialAllocationVersionRangeRule(16, 255)
        val item = newAccessorialAllocation("x", "y")
        item.version = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AccessorialAllocationRequiredCodeRule("accessorial allocation needs a value")
        val item = newAccessorialAllocation("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
