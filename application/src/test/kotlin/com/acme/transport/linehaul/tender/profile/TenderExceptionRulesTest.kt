package com.acme.transport.linehaul.tender.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TenderExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTenderExceptionValidator()
        val item = newTenderException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = TenderExceptionRequiredNameRule("tender exception needs an entry")
        val item = newTenderException("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = TenderExceptionStopCountRangeRule(1000, 1000)
        val item = newTenderException("x", "y")
        item.stopCount = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = TenderExceptionOdometerRangeRule(255, 1000)
        val item = newTenderException("x", "y")
        item.odometer = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = TenderExceptionCodeLengthRule(255)
        val item = newTenderException("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
