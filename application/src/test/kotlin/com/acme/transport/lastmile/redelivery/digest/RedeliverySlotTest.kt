package com.acme.transport.lastmile.redelivery.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RedeliverySlotTest {
    @Test
    fun `starts in created`() {
        val item = newRedeliverySlot("code-817", "label-461")
        assertEquals(RedeliverySlotStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newRedeliverySlot("code-817", "label-461")
        try {
            item.moveTo(RedeliverySlotStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RedeliverySlotStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRedeliverySlot("code-817", "label-461")
        item.status = RedeliverySlotStatus.RETIRED
        assertFails("move") { item.moveTo(RedeliverySlotStatus.CREATED) }
    }
}
