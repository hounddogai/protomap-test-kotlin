package com.acme.transport.terminal.sortation.sortationreview

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimarySortationSnapshotTest {
    @Test
    fun `starts in planned`() {
        val item = newPrimarySortationSnapshot("code-285", "label-767")
        assertEquals(PrimarySortationSnapshotStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newPrimarySortationSnapshot("code-285", "label-767")
        try {
            item.moveTo(PrimarySortationSnapshotStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimarySortationSnapshotStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newPrimarySortationSnapshot("code-285", "label-767")
        item.status = PrimarySortationSnapshotStatus.CANCELLED
        assertFails("move") { item.moveTo(PrimarySortationSnapshotStatus.PLANNED) }
    }
}
