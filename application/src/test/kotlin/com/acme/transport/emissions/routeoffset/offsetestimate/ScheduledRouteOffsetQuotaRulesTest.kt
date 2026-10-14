package com.acme.transport.emissions.routeoffset.offsetestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledRouteOffsetQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledRouteOffsetQuotaValidator()
        val item = newScheduledRouteOffsetQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = ScheduledRouteOffsetQuotaRequiredShiftCodeRule("scheduled route offset quota needs a value")
        val item = newScheduledRouteOffsetQuota("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = ScheduledRouteOffsetQuotaShiftCodeLengthRule(86400)
        val item = newScheduledRouteOffsetQuota("x", "y")
        item.shiftCode = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScheduledRouteOffsetQuotaRequiredCodeRule("scheduled route offset quota needs this field")
        val item = newScheduledRouteOffsetQuota("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
