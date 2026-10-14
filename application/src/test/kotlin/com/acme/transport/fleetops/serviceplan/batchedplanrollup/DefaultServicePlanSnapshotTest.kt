package com.acme.transport.fleetops.serviceplan.batchedplanrollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultServicePlanSnapshotTest {
    @Test
    fun `starts in open`() {
        val item = newDefaultServicePlanSnapshot("code-545", "title-88")
        assertEquals(DefaultServicePlanSnapshotStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newDefaultServicePlanSnapshot("code-545", "title-88")
        try {
            item.moveTo(DefaultServicePlanSnapshotStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultServicePlanSnapshotStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newDefaultServicePlanSnapshot("code-545", "title-88")
        item.status = DefaultServicePlanSnapshotStatus.ARCHIVED
        assertFails("move") { item.moveTo(DefaultServicePlanSnapshotStatus.OPEN) }
    }
}
