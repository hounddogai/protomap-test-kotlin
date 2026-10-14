package com.acme.transport.emissions.routeoffset.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RouteOffsetTemplateTest {
    @Test
    fun `starts in new`() {
        val item = newRouteOffsetTemplate("code-620", "name-672")
        assertEquals(RouteOffsetTemplateStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newRouteOffsetTemplate("code-620", "name-672")
        try {
            item.moveTo(RouteOffsetTemplateStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RouteOffsetTemplateStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRouteOffsetTemplate("code-620", "name-672")
        item.status = RouteOffsetTemplateStatus.FAILED
        assertFails("move") { item.moveTo(RouteOffsetTemplateStatus.NEW) }
    }
}
