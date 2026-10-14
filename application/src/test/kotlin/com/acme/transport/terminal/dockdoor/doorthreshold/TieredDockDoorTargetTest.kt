package com.acme.transport.terminal.dockdoor.doorthreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredDockDoorTargetTest {
    @Test
    fun `starts in created`() {
        val item = newTieredDockDoorTarget("code-384", "label-582")
        assertEquals(TieredDockDoorTargetStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newTieredDockDoorTarget("code-384", "label-582")
        try {
            item.moveTo(TieredDockDoorTargetStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredDockDoorTargetStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newTieredDockDoorTarget("code-384", "label-582")
        item.status = TieredDockDoorTargetStatus.RETIRED
        assertFails("move") { item.moveTo(TieredDockDoorTargetStatus.CREATED) }
    }
}
