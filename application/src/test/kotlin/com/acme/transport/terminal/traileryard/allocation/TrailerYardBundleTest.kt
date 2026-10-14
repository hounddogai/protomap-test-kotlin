package com.acme.transport.terminal.traileryard.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TrailerYardBundleTest {
    @Test
    fun `starts in idle`() {
        val item = newTrailerYardBundle("code-271", "name-541")
        assertEquals(TrailerYardBundleStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newTrailerYardBundle("code-271", "name-541")
        try {
            item.moveTo(TrailerYardBundleStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TrailerYardBundleStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newTrailerYardBundle("code-271", "name-541")
        item.status = TrailerYardBundleStatus.STOPPED
        assertFails("move") { item.moveTo(TrailerYardBundleStatus.IDLE) }
    }
}
