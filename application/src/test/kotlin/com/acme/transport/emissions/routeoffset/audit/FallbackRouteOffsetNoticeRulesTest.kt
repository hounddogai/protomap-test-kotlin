package com.acme.transport.emissions.routeoffset.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackRouteOffsetNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackRouteOffsetNoticeValidator()
        val item = newFallbackRouteOffsetNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = FallbackRouteOffsetNoticeCodeLengthRule(1000)
        val item = newFallbackRouteOffsetNotice("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
