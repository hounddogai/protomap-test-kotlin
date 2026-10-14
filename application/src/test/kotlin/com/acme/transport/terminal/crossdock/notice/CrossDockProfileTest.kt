package com.acme.transport.terminal.crossdock.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CrossDockProfileTest {
    @Test
    fun `starts in proposed`() {
        val item = newCrossDockProfile("code-685", "title-823")
        assertEquals(CrossDockProfileStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newCrossDockProfile("code-685", "title-823")
        try {
            item.moveTo(CrossDockProfileStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CrossDockProfileStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newCrossDockProfile("code-685", "title-823")
        item.status = CrossDockProfileStatus.WITHDRAWN
        assertFails("move") { item.moveTo(CrossDockProfileStatus.PROPOSED) }
    }
}
