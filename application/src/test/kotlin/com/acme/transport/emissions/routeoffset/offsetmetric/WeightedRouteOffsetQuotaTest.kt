package com.acme.transport.emissions.routeoffset.offsetmetric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedRouteOffsetQuotaTest {
    @Test
    fun `starts in draft`() {
        val item = newWeightedRouteOffsetQuota("code-346", "label-296")
        assertEquals(WeightedRouteOffsetQuotaStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newWeightedRouteOffsetQuota("code-346", "label-296")
        try {
            item.moveTo(WeightedRouteOffsetQuotaStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedRouteOffsetQuotaStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newWeightedRouteOffsetQuota("code-346", "label-296")
        item.status = WeightedRouteOffsetQuotaStatus.CLOSED
        assertFails("move") { item.moveTo(WeightedRouteOffsetQuotaStatus.DRAFT) }
    }
}
