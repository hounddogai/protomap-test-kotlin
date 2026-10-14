package com.acme.transport.fleetops.serviceplan.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackServicePlanRequestTest {
    @Test
    fun `starts in created`() {
        val item = newFallbackServicePlanRequest("code-519", "name-490")
        assertEquals(FallbackServicePlanRequestStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newFallbackServicePlanRequest("code-519", "name-490")
        try {
            item.moveTo(FallbackServicePlanRequestStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackServicePlanRequestStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newFallbackServicePlanRequest("code-519", "name-490")
        item.status = FallbackServicePlanRequestStatus.RETIRED
        assertFails("move") { item.moveTo(FallbackServicePlanRequestStatus.CREATED) }
    }
}
