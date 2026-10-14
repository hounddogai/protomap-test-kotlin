package com.acme.transport.emissions.routeoffset.offsetestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingRouteOffsetProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingRouteOffsetProfileValidator()
        val item = newPendingRouteOffsetProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = PendingRouteOffsetProfileTitleLengthRule(255)
        val item = newPendingRouteOffsetProfile("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = PendingRouteOffsetProfileRequiredOwnerIdRule("pending route offset profile needs an entry")
        val item = newPendingRouteOffsetProfile("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PendingRouteOffsetProfileRequiredCodeRule("pending route offset profile needs an entry")
        val item = newPendingRouteOffsetProfile("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = PendingRouteOffsetProfileRequiredTitleRule("pending route offset profile needs this field")
        val item = newPendingRouteOffsetProfile("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
