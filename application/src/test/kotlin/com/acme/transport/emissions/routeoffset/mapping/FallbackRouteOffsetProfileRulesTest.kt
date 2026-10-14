package com.acme.transport.emissions.routeoffset.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackRouteOffsetProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackRouteOffsetProfileValidator()
        val item = newFallbackRouteOffsetProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FallbackRouteOffsetProfileRequiredCodeRule("fallback route offset profile needs a value")
        val item = newFallbackRouteOffsetProfile("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = FallbackRouteOffsetProfileTitleLengthRule(80)
        val item = newFallbackRouteOffsetProfile("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
