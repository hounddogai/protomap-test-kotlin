package com.acme.transport.emissions.idling.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalIdlingSnapshotTest {
    @Test
    fun `starts in draft`() {
        val item = newRegionalIdlingSnapshot("code-760", "label-362")
        assertEquals(RegionalIdlingSnapshotStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newRegionalIdlingSnapshot("code-760", "label-362")
        try {
            item.moveTo(RegionalIdlingSnapshotStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalIdlingSnapshotStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newRegionalIdlingSnapshot("code-760", "label-362")
        item.status = RegionalIdlingSnapshotStatus.CLOSED
        assertFails("move") { item.moveTo(RegionalIdlingSnapshotStatus.DRAFT) }
    }
}
