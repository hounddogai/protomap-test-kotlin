package com.acme.transport.emissions.routeoffset.offsetestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledRouteOffsetQuotaTest {
    @Test
    fun `starts in idle`() {
        val item = newScheduledRouteOffsetQuota("code-292", "title-502")
        assertEquals(ScheduledRouteOffsetQuotaStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newScheduledRouteOffsetQuota("code-292", "title-502")
        try {
            item.moveTo(ScheduledRouteOffsetQuotaStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledRouteOffsetQuotaStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newScheduledRouteOffsetQuota("code-292", "title-502")
        item.status = ScheduledRouteOffsetQuotaStatus.STOPPED
        assertFails("move") { item.moveTo(ScheduledRouteOffsetQuotaStatus.IDLE) }
    }
}
