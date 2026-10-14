package com.acme.transport.terminal.crossdock.dockproposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredCrossDockBaselineTest {
    @Test
    fun `starts in planned`() {
        val item = newTieredCrossDockBaseline("code-489", "name-213")
        assertEquals(TieredCrossDockBaselineStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newTieredCrossDockBaseline("code-489", "name-213")
        try {
            item.moveTo(TieredCrossDockBaselineStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredCrossDockBaselineStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newTieredCrossDockBaseline("code-489", "name-213")
        item.status = TieredCrossDockBaselineStatus.CANCELLED
        assertFails("move") { item.moveTo(TieredCrossDockBaselineStatus.PLANNED) }
    }
}
