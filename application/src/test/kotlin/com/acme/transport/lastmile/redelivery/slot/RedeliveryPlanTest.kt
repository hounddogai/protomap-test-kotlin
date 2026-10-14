package com.acme.transport.lastmile.redelivery.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RedeliveryPlanTest {
    @Test
    fun `starts in requested`() {
        val item = newRedeliveryPlan("code-759", "label-98")
        assertEquals(RedeliveryPlanStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newRedeliveryPlan("code-759", "label-98")
        try {
            item.moveTo(RedeliveryPlanStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RedeliveryPlanStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRedeliveryPlan("code-759", "label-98")
        item.status = RedeliveryPlanStatus.EXPIRED
        assertFails("move") { item.moveTo(RedeliveryPlanStatus.REQUESTED) }
    }
}
