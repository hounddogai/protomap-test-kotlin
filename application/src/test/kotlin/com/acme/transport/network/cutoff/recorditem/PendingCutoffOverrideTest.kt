package com.acme.transport.network.cutoff.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingCutoffOverrideTest {
    @Test
    fun `starts in planned`() {
        val item = newPendingCutoffOverride("code-863", "title-881")
        assertEquals(PendingCutoffOverrideStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newPendingCutoffOverride("code-863", "title-881")
        try {
            item.moveTo(PendingCutoffOverrideStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingCutoffOverrideStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newPendingCutoffOverride("code-863", "title-881")
        item.status = PendingCutoffOverrideStatus.CANCELLED
        assertFails("move") { item.moveTo(PendingCutoffOverrideStatus.PLANNED) }
    }
}
