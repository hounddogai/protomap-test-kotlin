package com.acme.transport.terminal.dwell.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredDwellGroupTest {
    @Test
    fun `starts in idle`() {
        val item = newTieredDwellGroup("code-582", "label-811")
        assertEquals(TieredDwellGroupStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newTieredDwellGroup("code-582", "label-811")
        try {
            item.moveTo(TieredDwellGroupStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredDwellGroupStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newTieredDwellGroup("code-582", "label-811")
        item.status = TieredDwellGroupStatus.STOPPED
        assertFails("move") { item.moveTo(TieredDwellGroupStatus.IDLE) }
    }
}
