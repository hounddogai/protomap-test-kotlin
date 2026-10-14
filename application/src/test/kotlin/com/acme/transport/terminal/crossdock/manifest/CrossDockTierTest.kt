package com.acme.transport.terminal.crossdock.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CrossDockTierTest {
    @Test
    fun `starts in created`() {
        val item = newCrossDockTier("code-511", "name-675")
        assertEquals(CrossDockTierStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newCrossDockTier("code-511", "name-675")
        try {
            item.moveTo(CrossDockTierStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CrossDockTierStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newCrossDockTier("code-511", "name-675")
        item.status = CrossDockTierStatus.RETIRED
        assertFails("move") { item.moveTo(CrossDockTierStatus.CREATED) }
    }
}
