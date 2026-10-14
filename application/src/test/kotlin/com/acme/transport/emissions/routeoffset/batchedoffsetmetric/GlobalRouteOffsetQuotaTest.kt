package com.acme.transport.emissions.routeoffset.batchedoffsetmetric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalRouteOffsetQuotaTest {
    @Test
    fun `starts in requested`() {
        val item = newGlobalRouteOffsetQuota("code-80", "label-776")
        assertEquals(GlobalRouteOffsetQuotaStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newGlobalRouteOffsetQuota("code-80", "label-776")
        try {
            item.moveTo(GlobalRouteOffsetQuotaStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalRouteOffsetQuotaStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newGlobalRouteOffsetQuota("code-80", "label-776")
        item.status = GlobalRouteOffsetQuotaStatus.EXPIRED
        assertFails("move") { item.moveTo(GlobalRouteOffsetQuotaStatus.REQUESTED) }
    }
}
