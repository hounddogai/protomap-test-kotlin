package com.acme.transport.emissions.routeoffset.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RouteOffsetMappingTest {
    @Test
    fun `starts in draft`() {
        val item = newRouteOffsetMapping("code-177", "title-997")
        assertEquals(RouteOffsetMappingStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newRouteOffsetMapping("code-177", "title-997")
        try {
            item.moveTo(RouteOffsetMappingStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RouteOffsetMappingStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newRouteOffsetMapping("code-177", "title-997")
        item.status = RouteOffsetMappingStatus.CLOSED
        assertFails("move") { item.moveTo(RouteOffsetMappingStatus.DRAFT) }
    }
}
