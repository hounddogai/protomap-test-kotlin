package com.acme.transport.fleetops.tire.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingTireBaselineTest {
    @Test
    fun `starts in draft`() {
        val item = newPendingTireBaseline("code-475", "label-671")
        assertEquals(PendingTireBaselineStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newPendingTireBaseline("code-475", "label-671")
        try {
            item.moveTo(PendingTireBaselineStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingTireBaselineStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newPendingTireBaseline("code-475", "label-671")
        item.status = PendingTireBaselineStatus.CLOSED
        assertFails("move") { item.moveTo(PendingTireBaselineStatus.DRAFT) }
    }
}
