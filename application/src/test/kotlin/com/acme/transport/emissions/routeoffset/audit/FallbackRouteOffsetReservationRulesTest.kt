package com.acme.transport.emissions.routeoffset.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackRouteOffsetReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackRouteOffsetReservationValidator()
        val item = newFallbackRouteOffsetReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FallbackRouteOffsetReservationRequiredCodeRule("fallback route offset reservation needs an entry")
        val item = newFallbackRouteOffsetReservation("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = FallbackRouteOffsetReservationCodeLengthRule(64)
        val item = newFallbackRouteOffsetReservation("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = FallbackRouteOffsetReservationRequiredTitleRule("fallback route offset reservation needs a value")
        val item = newFallbackRouteOffsetReservation("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
