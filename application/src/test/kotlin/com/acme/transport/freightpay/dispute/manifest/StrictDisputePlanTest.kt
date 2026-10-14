package com.acme.transport.freightpay.dispute.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictDisputePlanTest {
    @Test
    fun `starts in proposed`() {
        val item = newStrictDisputePlan("code-766", "title-425")
        assertEquals(StrictDisputePlanStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newStrictDisputePlan("code-766", "title-425")
        try {
            item.moveTo(StrictDisputePlanStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictDisputePlanStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newStrictDisputePlan("code-766", "title-425")
        item.status = StrictDisputePlanStatus.WITHDRAWN
        assertFails("move") { item.moveTo(StrictDisputePlanStatus.PROPOSED) }
    }
}
