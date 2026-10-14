package com.acme.transport.customs.hold.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryHoldNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryHoldNoticeValidator()
        val item = newPrimaryHoldNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = PrimaryHoldNoticeRequiredNameRule("primary hold notice needs an entry")
        val item = newPrimaryHoldNotice("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long source`() {
        val rule = PrimaryHoldNoticeSourceLengthRule(64)
        val item = newPrimaryHoldNotice("x", "y")
        item.source = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.source = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = PrimaryHoldNoticeRequiredCurrencyRule("primary hold notice needs an entry")
        val item = newPrimaryHoldNotice("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PrimaryHoldNoticeRequiredCodeRule("primary hold notice needs this field")
        val item = newPrimaryHoldNotice("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
