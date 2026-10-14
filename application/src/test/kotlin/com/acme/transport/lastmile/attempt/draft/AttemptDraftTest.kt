package com.acme.transport.lastmile.attempt.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AttemptDraftTest {
    @Test
    fun `starts in proposed`() {
        val item = newAttemptDraft("code-799", "title-569")
        assertEquals(AttemptDraftStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newAttemptDraft("code-799", "title-569")
        try {
            item.moveTo(AttemptDraftStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AttemptDraftStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newAttemptDraft("code-799", "title-569")
        item.status = AttemptDraftStatus.WITHDRAWN
        assertFails("move") { item.moveTo(AttemptDraftStatus.PROPOSED) }
    }
}
