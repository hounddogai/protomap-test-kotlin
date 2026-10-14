package com.acme.transport.crewing.swap.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalSwapEntryTest {
    @Test
    fun `starts in draft`() {
        val item = newRegionalSwapEntry("code-506", "title-288")
        assertEquals(RegionalSwapEntryStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newRegionalSwapEntry("code-506", "title-288")
        try {
            item.moveTo(RegionalSwapEntryStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalSwapEntryStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newRegionalSwapEntry("code-506", "title-288")
        item.status = RegionalSwapEntryStatus.CLOSED
        assertFails("move") { item.moveTo(RegionalSwapEntryStatus.DRAFT) }
    }
}
