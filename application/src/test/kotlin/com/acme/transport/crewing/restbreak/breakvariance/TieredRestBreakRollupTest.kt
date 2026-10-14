package com.acme.transport.crewing.restbreak.breakvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredRestBreakRollupTest {
    @Test
    fun `starts in idle`() {
        val item = newTieredRestBreakRollup("code-979", "label-741")
        assertEquals(TieredRestBreakRollupStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newTieredRestBreakRollup("code-979", "label-741")
        try {
            item.moveTo(TieredRestBreakRollupStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredRestBreakRollupStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newTieredRestBreakRollup("code-979", "label-741")
        item.status = TieredRestBreakRollupStatus.STOPPED
        assertFails("move") { item.moveTo(TieredRestBreakRollupStatus.IDLE) }
    }
}
