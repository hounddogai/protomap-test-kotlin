package com.acme.transport.emissions.routeoffset.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RouteOffsetQuotaTest {
    @Test
    fun `starts in draft`() {
        val item = newRouteOffsetQuota("code-801", "label-655")
        assertEquals(RouteOffsetQuotaStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newRouteOffsetQuota("code-801", "label-655")
        try {
            item.moveTo(RouteOffsetQuotaStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RouteOffsetQuotaStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newRouteOffsetQuota("code-801", "label-655")
        item.status = RouteOffsetQuotaStatus.CLOSED
        assertFails("move") { item.moveTo(RouteOffsetQuotaStatus.DRAFT) }
    }
}
