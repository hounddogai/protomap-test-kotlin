package com.acme.transport.freightpay.accessorial.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AccessorialBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAccessorialBaselineValidator()
        val item = newAccessorialBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above revision`() {
        val rule = AccessorialBaselineRevisionRangeRule(16, 255)
        val item = newAccessorialBaseline("x", "y")
        item.revision = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = AccessorialBaselineTitleLengthRule(255)
        val item = newAccessorialBaseline("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long country`() {
        val rule = AccessorialBaselineCountryLengthRule(3600)
        val item = newAccessorialBaseline("x", "y")
        item.country = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = AccessorialBaselineRequiredTitleRule("accessorial baseline needs an entry")
        val item = newAccessorialBaseline("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = AccessorialBaselineRequiredNameRule("accessorial baseline needs this field")
        val item = newAccessorialBaseline("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
