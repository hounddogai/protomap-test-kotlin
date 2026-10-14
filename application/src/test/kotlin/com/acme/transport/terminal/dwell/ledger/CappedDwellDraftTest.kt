package com.acme.transport.terminal.dwell.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedDwellDraftTest {
    @Test
    fun `starts in created`() {
        val item = newCappedDwellDraft("code-734", "name-58")
        assertEquals(CappedDwellDraftStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newCappedDwellDraft("code-734", "name-58")
        try {
            item.moveTo(CappedDwellDraftStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedDwellDraftStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newCappedDwellDraft("code-734", "name-58")
        item.status = CappedDwellDraftStatus.RETIRED
        assertFails("move") { item.moveTo(CappedDwellDraftStatus.CREATED) }
    }
}
