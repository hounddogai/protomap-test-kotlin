package com.acme.transport.terminal.crossdock.docknotice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultCrossDockPreferenceTest {
    @Test
    fun `starts in proposed`() {
        val item = newDefaultCrossDockPreference("code-15", "name-532")
        assertEquals(DefaultCrossDockPreferenceStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newDefaultCrossDockPreference("code-15", "name-532")
        try {
            item.moveTo(DefaultCrossDockPreferenceStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultCrossDockPreferenceStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newDefaultCrossDockPreference("code-15", "name-532")
        item.status = DefaultCrossDockPreferenceStatus.WITHDRAWN
        assertFails("move") { item.moveTo(DefaultCrossDockPreferenceStatus.PROPOSED) }
    }
}
