package com.acme.transport.emissions.idling.idlingvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class IdlingDraftTest {
    @Test
    fun `starts in new`() {
        val item = newIdlingDraft("code-338", "label-568")
        assertEquals(IdlingDraftStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newIdlingDraft("code-338", "label-568")
        try {
            item.moveTo(IdlingDraftStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(IdlingDraftStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newIdlingDraft("code-338", "label-568")
        item.status = IdlingDraftStatus.FAILED
        assertFails("move") { item.moveTo(IdlingDraftStatus.NEW) }
    }
}
