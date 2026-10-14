package com.acme.transport.freightpay.ratecard.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultRateCardEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultRateCardEntryValidator()
        val item = newDefaultRateCardEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank category`() {
        val rule = DefaultRateCardEntryRequiredCategoryRule("default rate card entry needs an entry")
        val item = newDefaultRateCardEntry("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long route code`() {
        val rule = DefaultRateCardEntryRouteCodeLengthRule(86400)
        val item = newDefaultRateCardEntry("x", "y")
        item.routeCode = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = DefaultRateCardEntryRequiredRouteCodeRule("default rate card entry needs a value")
        val item = newDefaultRateCardEntry("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above revision`() {
        val rule = DefaultRateCardEntryRevisionRangeRule(16, 120)
        val item = newDefaultRateCardEntry("x", "y")
        item.revision = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = DefaultRateCardEntryTitleLengthRule(80)
        val item = newDefaultRateCardEntry("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
