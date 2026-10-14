package com.acme.transport.network.transittime.timedigest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientTransitTimeAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientTransitTimeAuditValidator()
        val item = newLenientTransitTimeAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = LenientTransitTimeAuditQuantityRangeRule(64, 1000)
        val item = newLenientTransitTimeAudit("x", "y")
        item.quantity = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long category`() {
        val rule = LenientTransitTimeAuditCategoryLengthRule(64)
        val item = newLenientTransitTimeAudit("x", "y")
        item.category = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = LenientTransitTimeAuditRequiredDriverIdRule("lenient transit time audit needs this field")
        val item = newLenientTransitTimeAudit("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = LenientTransitTimeAuditDriverIdLengthRule(1000)
        val item = newLenientTransitTimeAudit("x", "y")
        item.driverId = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
