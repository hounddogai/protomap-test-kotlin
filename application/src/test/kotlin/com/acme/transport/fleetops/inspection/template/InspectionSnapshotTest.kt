package com.acme.transport.fleetops.inspection.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionSnapshotTest {
    @Test
    fun `starts in open`() {
        val item = newInspectionSnapshot("code-352", "label-302")
        assertEquals(InspectionSnapshotStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newInspectionSnapshot("code-352", "label-302")
        try {
            item.moveTo(InspectionSnapshotStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionSnapshotStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newInspectionSnapshot("code-352", "label-302")
        item.status = InspectionSnapshotStatus.ARCHIVED
        assertFails("move") { item.moveTo(InspectionSnapshotStatus.OPEN) }
    }
}
