package com.acme.transport.lastmile.redelivery.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RedeliveryAdjustmentTest {
    @Test
    fun `starts in created`() {
        val item = newRedeliveryAdjustment("code-976", "name-724")
        assertEquals(RedeliveryAdjustmentStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newRedeliveryAdjustment("code-976", "name-724")
        try {
            item.moveTo(RedeliveryAdjustmentStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RedeliveryAdjustmentStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRedeliveryAdjustment("code-976", "name-724")
        item.status = RedeliveryAdjustmentStatus.RETIRED
        assertFails("move") { item.moveTo(RedeliveryAdjustmentStatus.CREATED) }
    }
}
