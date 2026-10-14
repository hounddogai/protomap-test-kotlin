package com.acme.transport.network.cutoff.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CutoffCycleTest {
    @Test
    fun `starts in open`() {
        val item = newCutoffCycle("code-556", "label-527")
        assertEquals(CutoffCycleStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newCutoffCycle("code-556", "label-527")
        try {
            item.moveTo(CutoffCycleStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CutoffCycleStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newCutoffCycle("code-556", "label-527")
        item.status = CutoffCycleStatus.ARCHIVED
        assertFails("move") { item.moveTo(CutoffCycleStatus.OPEN) }
    }
}
