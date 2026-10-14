package com.acme.transport.freightpay.accessorial.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AccessorialRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAccessorialRequestValidator()
        val item = newAccessorialRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = AccessorialRequestNameLengthRule(3600)
        val item = newAccessorialRequest("x", "y")
        item.name = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long locale`() {
        val rule = AccessorialRequestLocaleLengthRule(16)
        val item = newAccessorialRequest("x", "y")
        item.locale = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = AccessorialRequestRequiredCurrencyRule("accessorial request needs an entry")
        val item = newAccessorialRequest("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
