package com.acme.transport.terminal.traileryard.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultTrailerYardWindowTest {
    @Test
    fun `starts in idle`() {
        val item = newDefaultTrailerYardWindow("code-688", "label-97")
        assertEquals(DefaultTrailerYardWindowStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newDefaultTrailerYardWindow("code-688", "label-97")
        try {
            item.moveTo(DefaultTrailerYardWindowStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultTrailerYardWindowStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newDefaultTrailerYardWindow("code-688", "label-97")
        item.status = DefaultTrailerYardWindowStatus.STOPPED
        assertFails("move") { item.moveTo(DefaultTrailerYardWindowStatus.IDLE) }
    }
}
