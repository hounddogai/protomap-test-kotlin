package com.acme.transport.emissions.routeoffset.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RouteOffsetRuleTest {
    @Test
    fun `starts in requested`() {
        val item = newRouteOffsetRule("code-40", "title-763")
        assertEquals(RouteOffsetRuleStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newRouteOffsetRule("code-40", "title-763")
        try {
            item.moveTo(RouteOffsetRuleStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RouteOffsetRuleStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRouteOffsetRule("code-40", "title-763")
        item.status = RouteOffsetRuleStatus.EXPIRED
        assertFails("move") { item.moveTo(RouteOffsetRuleStatus.REQUESTED) }
    }
}
