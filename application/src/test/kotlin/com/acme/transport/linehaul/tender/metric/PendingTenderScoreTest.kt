package com.acme.transport.linehaul.tender.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingTenderScoreTest {
    @Test
    fun `starts in new`() {
        val item = newPendingTenderScore("code-644", "name-93")
        assertEquals(PendingTenderScoreStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newPendingTenderScore("code-644", "name-93")
        try {
            item.moveTo(PendingTenderScoreStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingTenderScoreStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newPendingTenderScore("code-644", "name-93")
        item.status = PendingTenderScoreStatus.FAILED
        assertFails("move") { item.moveTo(PendingTenderScoreStatus.NEW) }
    }
}
