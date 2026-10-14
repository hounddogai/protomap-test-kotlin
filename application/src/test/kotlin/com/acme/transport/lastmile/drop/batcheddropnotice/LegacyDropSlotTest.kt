package com.acme.transport.lastmile.drop.batcheddropnotice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyDropSlotTest {
    @Test
    fun `starts in proposed`() {
        val item = newLegacyDropSlot("code-564", "title-898")
        assertEquals(LegacyDropSlotStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newLegacyDropSlot("code-564", "title-898")
        try {
            item.moveTo(LegacyDropSlotStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyDropSlotStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newLegacyDropSlot("code-564", "title-898")
        item.status = LegacyDropSlotStatus.WITHDRAWN
        assertFails("move") { item.moveTo(LegacyDropSlotStatus.PROPOSED) }
    }
}
