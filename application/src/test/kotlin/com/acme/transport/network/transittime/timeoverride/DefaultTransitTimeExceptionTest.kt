package com.acme.transport.network.transittime.timeoverride

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultTransitTimeExceptionTest {
    @Test
    fun `starts in idle`() {
        val item = newDefaultTransitTimeException("code-41", "name-835")
        assertEquals(DefaultTransitTimeExceptionStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newDefaultTransitTimeException("code-41", "name-835")
        try {
            item.moveTo(DefaultTransitTimeExceptionStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultTransitTimeExceptionStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newDefaultTransitTimeException("code-41", "name-835")
        item.status = DefaultTransitTimeExceptionStatus.STOPPED
        assertFails("move") { item.moveTo(DefaultTransitTimeExceptionStatus.IDLE) }
    }
}
