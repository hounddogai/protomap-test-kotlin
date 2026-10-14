package com.acme.transport.network.cutoff.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CutoffTierTest {
    @Test
    fun `starts in new`() {
        val item = newCutoffTier("code-888", "label-658")
        assertEquals(CutoffTierStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newCutoffTier("code-888", "label-658")
        try {
            item.moveTo(CutoffTierStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CutoffTierStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newCutoffTier("code-888", "label-658")
        item.status = CutoffTierStatus.FAILED
        assertFails("move") { item.moveTo(CutoffTierStatus.NEW) }
    }
}
