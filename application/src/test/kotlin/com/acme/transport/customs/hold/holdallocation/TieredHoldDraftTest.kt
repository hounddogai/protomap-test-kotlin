package com.acme.transport.customs.hold.holdallocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredHoldDraftTest {
    @Test
    fun `starts in proposed`() {
        val item = newTieredHoldDraft("code-67", "label-629")
        assertEquals(TieredHoldDraftStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newTieredHoldDraft("code-67", "label-629")
        try {
            item.moveTo(TieredHoldDraftStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredHoldDraftStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newTieredHoldDraft("code-67", "label-629")
        item.status = TieredHoldDraftStatus.WITHDRAWN
        assertFails("move") { item.moveTo(TieredHoldDraftStatus.PROPOSED) }
    }
}
