package com.acme.transport.terminal.traileryard.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingTrailerYardWindowTest {
    @Test
    fun `starts in draft`() {
        val item = newRollingTrailerYardWindow("code-45", "label-59")
        assertEquals(RollingTrailerYardWindowStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newRollingTrailerYardWindow("code-45", "label-59")
        try {
            item.moveTo(RollingTrailerYardWindowStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingTrailerYardWindowStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newRollingTrailerYardWindow("code-45", "label-59")
        item.status = RollingTrailerYardWindowStatus.CLOSED
        assertFails("move") { item.moveTo(RollingTrailerYardWindowStatus.DRAFT) }
    }
}
