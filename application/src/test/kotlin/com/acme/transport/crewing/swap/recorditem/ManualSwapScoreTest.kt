package com.acme.transport.crewing.swap.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualSwapScoreTest {
    @Test
    fun `starts in draft`() {
        val item = newManualSwapScore("code-965", "title-396")
        assertEquals(ManualSwapScoreStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newManualSwapScore("code-965", "title-396")
        try {
            item.moveTo(ManualSwapScoreStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualSwapScoreStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newManualSwapScore("code-965", "title-396")
        item.status = ManualSwapScoreStatus.CLOSED
        assertFails("move") { item.moveTo(ManualSwapScoreStatus.DRAFT) }
    }
}
