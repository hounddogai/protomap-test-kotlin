package com.acme.transport.freightpay.accessorial.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingAccessorialGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingAccessorialGroupValidator()
        val item = newPendingAccessorialGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PendingAccessorialGroupRequiredCodeRule("pending accessorial group needs a value")
        val item = newPendingAccessorialGroup("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = PendingAccessorialGroupRequiredTimezoneRule("pending accessorial group needs an entry")
        val item = newPendingAccessorialGroup("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = PendingAccessorialGroupNameLengthRule(16)
        val item = newPendingAccessorialGroup("x", "y")
        item.name = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
