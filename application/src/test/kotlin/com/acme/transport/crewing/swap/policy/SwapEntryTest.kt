package com.acme.transport.crewing.swap.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SwapEntryTest {
    @Test
    fun `starts in open`() {
        val item = newSwapEntry("code-713", "name-210")
        assertEquals(SwapEntryStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newSwapEntry("code-713", "name-210")
        try {
            item.moveTo(SwapEntryStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SwapEntryStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newSwapEntry("code-713", "name-210")
        item.status = SwapEntryStatus.ARCHIVED
        assertFails("move") { item.moveTo(SwapEntryStatus.OPEN) }
    }
}
