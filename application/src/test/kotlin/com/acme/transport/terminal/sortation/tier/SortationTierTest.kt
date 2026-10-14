package com.acme.transport.terminal.sortation.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SortationTierTest {
    @Test
    fun `starts in proposed`() {
        val item = newSortationTier("code-326", "label-991")
        assertEquals(SortationTierStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newSortationTier("code-326", "label-991")
        try {
            item.moveTo(SortationTierStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SortationTierStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newSortationTier("code-326", "label-991")
        item.status = SortationTierStatus.WITHDRAWN
        assertFails("move") { item.moveTo(SortationTierStatus.PROPOSED) }
    }
}
