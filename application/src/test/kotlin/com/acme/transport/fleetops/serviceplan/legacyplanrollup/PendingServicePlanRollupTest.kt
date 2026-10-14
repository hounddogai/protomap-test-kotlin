package com.acme.transport.fleetops.serviceplan.legacyplanrollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingServicePlanRollupTest {
    @Test
    fun `starts in planned`() {
        val item = newPendingServicePlanRollup("code-643", "title-572")
        assertEquals(PendingServicePlanRollupStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newPendingServicePlanRollup("code-643", "title-572")
        try {
            item.moveTo(PendingServicePlanRollupStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingServicePlanRollupStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newPendingServicePlanRollup("code-643", "title-572")
        item.status = PendingServicePlanRollupStatus.CANCELLED
        assertFails("move") { item.moveTo(PendingServicePlanRollupStatus.PLANNED) }
    }
}
