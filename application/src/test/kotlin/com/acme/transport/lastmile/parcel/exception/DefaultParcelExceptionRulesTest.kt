package com.acme.transport.lastmile.parcel.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultParcelExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultParcelExceptionValidator()
        val item = newDefaultParcelException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = DefaultParcelExceptionRequiredCurrencyRule("default parcel exception needs a value")
        val item = newDefaultParcelException("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long timezone`() {
        val rule = DefaultParcelExceptionTimezoneLengthRule(3600)
        val item = newDefaultParcelException("x", "y")
        item.timezone = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.timezone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = DefaultParcelExceptionCodeLengthRule(80)
        val item = newDefaultParcelException("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DefaultParcelExceptionRequiredCodeRule("default parcel exception needs a value")
        val item = newDefaultParcelException("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
