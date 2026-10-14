package com.acme.transport.network.cutoff.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedCutoffTargetTest {
    @Test
    fun `starts in planned`() {
        val item = newCappedCutoffTarget("code-284", "label-739")
        assertEquals(CappedCutoffTargetStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newCappedCutoffTarget("code-284", "label-739")
        try {
            item.moveTo(CappedCutoffTargetStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedCutoffTargetStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newCappedCutoffTarget("code-284", "label-739")
        item.status = CappedCutoffTargetStatus.CANCELLED
        assertFails("move") { item.moveTo(CappedCutoffTargetStatus.PLANNED) }
    }
}
