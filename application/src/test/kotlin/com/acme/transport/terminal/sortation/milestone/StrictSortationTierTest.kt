package com.acme.transport.terminal.sortation.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictSortationTierTest {
    @Test
    fun `starts in requested`() {
        val item = newStrictSortationTier("code-729", "title-288")
        assertEquals(StrictSortationTierStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newStrictSortationTier("code-729", "title-288")
        try {
            item.moveTo(StrictSortationTierStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictSortationTierStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newStrictSortationTier("code-729", "title-288")
        item.status = StrictSortationTierStatus.EXPIRED
        assertFails("move") { item.moveTo(StrictSortationTierStatus.REQUESTED) }
    }
}
