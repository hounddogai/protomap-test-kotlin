package com.acme.transport.terminal.dockdoor.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackDockDoorGroupTest {
    @Test
    fun `starts in created`() {
        val item = newFallbackDockDoorGroup("code-65", "title-386")
        assertEquals(FallbackDockDoorGroupStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newFallbackDockDoorGroup("code-65", "title-386")
        try {
            item.moveTo(FallbackDockDoorGroupStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackDockDoorGroupStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newFallbackDockDoorGroup("code-65", "title-386")
        item.status = FallbackDockDoorGroupStatus.RETIRED
        assertFails("move") { item.moveTo(FallbackDockDoorGroupStatus.CREATED) }
    }
}
