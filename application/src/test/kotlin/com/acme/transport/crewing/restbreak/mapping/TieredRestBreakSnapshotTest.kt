package com.acme.transport.crewing.restbreak.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredRestBreakSnapshotTest {
    @Test
    fun `starts in requested`() {
        val item = newTieredRestBreakSnapshot("code-830", "name-947")
        assertEquals(TieredRestBreakSnapshotStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newTieredRestBreakSnapshot("code-830", "name-947")
        try {
            item.moveTo(TieredRestBreakSnapshotStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredRestBreakSnapshotStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newTieredRestBreakSnapshot("code-830", "name-947")
        item.status = TieredRestBreakSnapshotStatus.EXPIRED
        assertFails("move") { item.moveTo(TieredRestBreakSnapshotStatus.REQUESTED) }
    }
}
