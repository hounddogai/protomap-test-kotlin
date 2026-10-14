package com.acme.transport.terminal.crossdock.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CrossDockClaimTest {
    @Test
    fun `starts in new`() {
        val item = newCrossDockClaim("code-246", "title-582")
        assertEquals(CrossDockClaimStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newCrossDockClaim("code-246", "title-582")
        try {
            item.moveTo(CrossDockClaimStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CrossDockClaimStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newCrossDockClaim("code-246", "title-582")
        item.status = CrossDockClaimStatus.FAILED
        assertFails("move") { item.moveTo(CrossDockClaimStatus.NEW) }
    }
}
