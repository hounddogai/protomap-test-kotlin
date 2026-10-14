package com.acme.transport.network.cutoff.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CutoffSlotTest {
    @Test
    fun `starts in open`() {
        val item = newCutoffSlot("code-538", "title-149")
        assertEquals(CutoffSlotStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newCutoffSlot("code-538", "title-149")
        try {
            item.moveTo(CutoffSlotStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CutoffSlotStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newCutoffSlot("code-538", "title-149")
        item.status = CutoffSlotStatus.ARCHIVED
        assertFails("move") { item.moveTo(CutoffSlotStatus.OPEN) }
    }
}
