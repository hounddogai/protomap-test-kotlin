package com.acme.transport.lastmile.redelivery.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingRedeliverySlotTest {
    @Test
    fun `starts in proposed`() {
        val item = newPendingRedeliverySlot("code-677", "name-929")
        assertEquals(PendingRedeliverySlotStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newPendingRedeliverySlot("code-677", "name-929")
        try {
            item.moveTo(PendingRedeliverySlotStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingRedeliverySlotStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newPendingRedeliverySlot("code-677", "name-929")
        item.status = PendingRedeliverySlotStatus.WITHDRAWN
        assertFails("move") { item.moveTo(PendingRedeliverySlotStatus.PROPOSED) }
    }
}
