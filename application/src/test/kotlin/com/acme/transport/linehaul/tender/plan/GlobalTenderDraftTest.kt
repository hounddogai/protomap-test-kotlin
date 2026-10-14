package com.acme.transport.linehaul.tender.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalTenderDraftTest {
    @Test
    fun `starts in proposed`() {
        val item = newGlobalTenderDraft("code-8", "name-238")
        assertEquals(GlobalTenderDraftStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newGlobalTenderDraft("code-8", "name-238")
        try {
            item.moveTo(GlobalTenderDraftStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalTenderDraftStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newGlobalTenderDraft("code-8", "name-238")
        item.status = GlobalTenderDraftStatus.WITHDRAWN
        assertFails("move") { item.moveTo(GlobalTenderDraftStatus.PROPOSED) }
    }
}
