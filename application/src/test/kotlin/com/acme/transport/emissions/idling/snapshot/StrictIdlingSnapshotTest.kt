package com.acme.transport.emissions.idling.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictIdlingSnapshotTest {
    @Test
    fun `starts in open`() {
        val item = newStrictIdlingSnapshot("code-125", "label-112")
        assertEquals(StrictIdlingSnapshotStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newStrictIdlingSnapshot("code-125", "label-112")
        try {
            item.moveTo(StrictIdlingSnapshotStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictIdlingSnapshotStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newStrictIdlingSnapshot("code-125", "label-112")
        item.status = StrictIdlingSnapshotStatus.ARCHIVED
        assertFails("move") { item.moveTo(StrictIdlingSnapshotStatus.OPEN) }
    }
}
