package com.acme.transport.customs.clearance.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ClearanceSnapshotTest {
    @Test
    fun `starts in open`() {
        val item = newClearanceSnapshot("code-432", "title-318")
        assertEquals(ClearanceSnapshotStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newClearanceSnapshot("code-432", "title-318")
        try {
            item.moveTo(ClearanceSnapshotStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ClearanceSnapshotStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newClearanceSnapshot("code-432", "title-318")
        item.status = ClearanceSnapshotStatus.ARCHIVED
        assertFails("move") { item.moveTo(ClearanceSnapshotStatus.OPEN) }
    }
}
