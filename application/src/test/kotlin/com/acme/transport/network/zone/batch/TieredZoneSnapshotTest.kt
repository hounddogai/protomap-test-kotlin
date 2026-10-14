package com.acme.transport.network.zone.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredZoneSnapshotTest {
    @Test
    fun `starts in proposed`() {
        val item = newTieredZoneSnapshot("code-749", "label-348")
        assertEquals(TieredZoneSnapshotStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newTieredZoneSnapshot("code-749", "label-348")
        try {
            item.moveTo(TieredZoneSnapshotStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredZoneSnapshotStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newTieredZoneSnapshot("code-749", "label-348")
        item.status = TieredZoneSnapshotStatus.WITHDRAWN
        assertFails("move") { item.moveTo(TieredZoneSnapshotStatus.PROPOSED) }
    }
}
