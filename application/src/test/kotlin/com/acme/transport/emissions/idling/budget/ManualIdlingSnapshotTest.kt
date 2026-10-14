package com.acme.transport.emissions.idling.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualIdlingSnapshotTest {
    @Test
    fun `starts in draft`() {
        val item = newManualIdlingSnapshot("code-585", "label-284")
        assertEquals(ManualIdlingSnapshotStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newManualIdlingSnapshot("code-585", "label-284")
        try {
            item.moveTo(ManualIdlingSnapshotStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualIdlingSnapshotStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newManualIdlingSnapshot("code-585", "label-284")
        item.status = ManualIdlingSnapshotStatus.CLOSED
        assertFails("move") { item.moveTo(ManualIdlingSnapshotStatus.DRAFT) }
    }
}
