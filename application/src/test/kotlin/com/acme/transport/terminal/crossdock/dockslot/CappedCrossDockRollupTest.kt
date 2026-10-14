package com.acme.transport.terminal.crossdock.dockslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedCrossDockRollupTest {
    @Test
    fun `starts in requested`() {
        val item = newCappedCrossDockRollup("code-397", "name-950")
        assertEquals(CappedCrossDockRollupStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newCappedCrossDockRollup("code-397", "name-950")
        try {
            item.moveTo(CappedCrossDockRollupStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedCrossDockRollupStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newCappedCrossDockRollup("code-397", "name-950")
        item.status = CappedCrossDockRollupStatus.EXPIRED
        assertFails("move") { item.moveTo(CappedCrossDockRollupStatus.REQUESTED) }
    }
}
