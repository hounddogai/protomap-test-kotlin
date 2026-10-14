package com.acme.transport.terminal.crossdock.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CrossDockDigestTest {
    @Test
    fun `starts in open`() {
        val item = newCrossDockDigest("code-572", "title-879")
        assertEquals(CrossDockDigestStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newCrossDockDigest("code-572", "title-879")
        try {
            item.moveTo(CrossDockDigestStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CrossDockDigestStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newCrossDockDigest("code-572", "title-879")
        item.status = CrossDockDigestStatus.ARCHIVED
        assertFails("move") { item.moveTo(CrossDockDigestStatus.OPEN) }
    }
}
