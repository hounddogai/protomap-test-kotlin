package com.acme.transport.crewing.swap.swaprecord

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedSwapRecordTest {
    @Test
    fun `starts in open`() {
        val item = newCappedSwapRecord("code-689", "name-49")
        assertEquals(CappedSwapRecordStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newCappedSwapRecord("code-689", "name-49")
        try {
            item.moveTo(CappedSwapRecordStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedSwapRecordStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newCappedSwapRecord("code-689", "name-49")
        item.status = CappedSwapRecordStatus.ARCHIVED
        assertFails("move") { item.moveTo(CappedSwapRecordStatus.OPEN) }
    }
}
