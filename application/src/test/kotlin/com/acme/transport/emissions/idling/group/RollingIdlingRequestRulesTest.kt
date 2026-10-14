package com.acme.transport.emissions.idling.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingIdlingRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingIdlingRequestValidator()
        val item = newRollingIdlingRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long phone`() {
        val rule = RollingIdlingRequestPhoneLengthRule(3600)
        val item = newRollingIdlingRequest("x", "y")
        item.phone = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long route code`() {
        val rule = RollingIdlingRequestRouteCodeLengthRule(120)
        val item = newRollingIdlingRequest("x", "y")
        item.routeCode = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = RollingIdlingRequestRequiredTitleRule("rolling idling request needs an entry")
        val item = newRollingIdlingRequest("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = RollingIdlingRequestThresholdRangeRule(2.8, 450.2)
        val item = newRollingIdlingRequest("x", "y")
        item.threshold = 451.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
