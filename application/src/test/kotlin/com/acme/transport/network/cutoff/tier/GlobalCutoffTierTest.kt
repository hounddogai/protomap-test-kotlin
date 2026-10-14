package com.acme.transport.network.cutoff.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalCutoffTierTest {
    @Test
    fun `starts in draft`() {
        val item = newGlobalCutoffTier("code-832", "name-37")
        assertEquals(GlobalCutoffTierStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newGlobalCutoffTier("code-832", "name-37")
        try {
            item.moveTo(GlobalCutoffTierStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalCutoffTierStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newGlobalCutoffTier("code-832", "name-37")
        item.status = GlobalCutoffTierStatus.CLOSED
        assertFails("move") { item.moveTo(GlobalCutoffTierStatus.DRAFT) }
    }
}
