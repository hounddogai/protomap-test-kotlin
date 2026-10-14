package com.acme.transport.lastmile.drop.dropdraft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredDropRollupTest {
    @Test
    fun `starts in idle`() {
        val item = newTieredDropRollup("code-991", "label-57")
        assertEquals(TieredDropRollupStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newTieredDropRollup("code-991", "label-57")
        try {
            item.moveTo(TieredDropRollupStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredDropRollupStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newTieredDropRollup("code-991", "label-57")
        item.status = TieredDropRollupStatus.STOPPED
        assertFails("move") { item.moveTo(TieredDropRollupStatus.IDLE) }
    }
}
