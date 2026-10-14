package com.acme.transport.terminal.sortation.sortationdigest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingSortationDigestTest {
    @Test
    fun `starts in draft`() {
        val item = newPendingSortationDigest("code-515", "label-766")
        assertEquals(PendingSortationDigestStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newPendingSortationDigest("code-515", "label-766")
        try {
            item.moveTo(PendingSortationDigestStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingSortationDigestStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newPendingSortationDigest("code-515", "label-766")
        item.status = PendingSortationDigestStatus.CLOSED
        assertFails("move") { item.moveTo(PendingSortationDigestStatus.DRAFT) }
    }
}
