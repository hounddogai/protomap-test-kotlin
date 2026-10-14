package com.acme.transport.network.cutoff.cutofftarget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualCutoffDraftTest {
    @Test
    fun `starts in idle`() {
        val item = newManualCutoffDraft("code-389", "label-128")
        assertEquals(ManualCutoffDraftStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newManualCutoffDraft("code-389", "label-128")
        try {
            item.moveTo(ManualCutoffDraftStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualCutoffDraftStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newManualCutoffDraft("code-389", "label-128")
        item.status = ManualCutoffDraftStatus.STOPPED
        assertFails("move") { item.moveTo(ManualCutoffDraftStatus.IDLE) }
    }
}
