package com.acme.transport.fleetops.serviceplan.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ServicePlanSnapshotTest {
    @Test
    fun `starts in draft`() {
        val item = newServicePlanSnapshot("code-254", "label-406")
        assertEquals(ServicePlanSnapshotStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newServicePlanSnapshot("code-254", "label-406")
        try {
            item.moveTo(ServicePlanSnapshotStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ServicePlanSnapshotStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newServicePlanSnapshot("code-254", "label-406")
        item.status = ServicePlanSnapshotStatus.CLOSED
        assertFails("move") { item.moveTo(ServicePlanSnapshotStatus.DRAFT) }
    }
}
