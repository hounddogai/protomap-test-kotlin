package com.acme.transport.fleetops.serviceplan.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingServicePlanTargetTest {
    @Test
    fun `starts in open`() {
        val item = newPendingServicePlanTarget("code-926", "name-302")
        assertEquals(PendingServicePlanTargetStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newPendingServicePlanTarget("code-926", "name-302")
        try {
            item.moveTo(PendingServicePlanTargetStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingServicePlanTargetStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newPendingServicePlanTarget("code-926", "name-302")
        item.status = PendingServicePlanTargetStatus.ARCHIVED
        assertFails("move") { item.moveTo(PendingServicePlanTargetStatus.OPEN) }
    }
}
