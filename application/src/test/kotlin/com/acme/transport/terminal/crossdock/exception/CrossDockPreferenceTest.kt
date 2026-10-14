package com.acme.transport.terminal.crossdock.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CrossDockPreferenceTest {
    @Test
    fun `starts in idle`() {
        val item = newCrossDockPreference("code-350", "title-600")
        assertEquals(CrossDockPreferenceStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newCrossDockPreference("code-350", "title-600")
        try {
            item.moveTo(CrossDockPreferenceStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CrossDockPreferenceStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newCrossDockPreference("code-350", "title-600")
        item.status = CrossDockPreferenceStatus.STOPPED
        assertFails("move") { item.moveTo(CrossDockPreferenceStatus.IDLE) }
    }
}
