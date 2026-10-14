package com.acme.transport.network.cutoff.cutoffsegment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CutoffTargetTest {
    @Test
    fun `starts in planned`() {
        val item = newCutoffTarget("code-32", "name-642")
        assertEquals(CutoffTargetStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newCutoffTarget("code-32", "name-642")
        try {
            item.moveTo(CutoffTargetStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CutoffTargetStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newCutoffTarget("code-32", "name-642")
        item.status = CutoffTargetStatus.CANCELLED
        assertFails("move") { item.moveTo(CutoffTargetStatus.PLANNED) }
    }
}
