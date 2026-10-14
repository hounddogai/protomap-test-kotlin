package com.acme.transport.network.cutoff.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CutoffGroupTest {
    @Test
    fun `starts in draft`() {
        val item = newCutoffGroup("code-922", "label-738")
        assertEquals(CutoffGroupStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newCutoffGroup("code-922", "label-738")
        try {
            item.moveTo(CutoffGroupStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CutoffGroupStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newCutoffGroup("code-922", "label-738")
        item.status = CutoffGroupStatus.CLOSED
        assertFails("move") { item.moveTo(CutoffGroupStatus.DRAFT) }
    }
}
