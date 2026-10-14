package com.acme.transport.crewing.restbreak.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictRestBreakTierTest {
    @Test
    fun `starts in open`() {
        val item = newStrictRestBreakTier("code-859", "title-142")
        assertEquals(StrictRestBreakTierStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newStrictRestBreakTier("code-859", "title-142")
        try {
            item.moveTo(StrictRestBreakTierStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictRestBreakTierStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newStrictRestBreakTier("code-859", "title-142")
        item.status = StrictRestBreakTierStatus.ARCHIVED
        assertFails("move") { item.moveTo(StrictRestBreakTierStatus.OPEN) }
    }
}
