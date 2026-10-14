package com.acme.transport.crewing.restbreak.regionalbreakpreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingRestBreakRollupTest {
    @Test
    fun `starts in idle`() {
        val item = newPendingRestBreakRollup("code-366", "name-679")
        assertEquals(PendingRestBreakRollupStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newPendingRestBreakRollup("code-366", "name-679")
        try {
            item.moveTo(PendingRestBreakRollupStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingRestBreakRollupStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newPendingRestBreakRollup("code-366", "name-679")
        item.status = PendingRestBreakRollupStatus.STOPPED
        assertFails("move") { item.moveTo(PendingRestBreakRollupStatus.IDLE) }
    }
}
