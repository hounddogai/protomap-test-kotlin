package com.acme.transport.lastmile.redelivery.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RedeliveryEstimateTest {
    @Test
    fun `starts in planned`() {
        val item = newRedeliveryEstimate("code-240", "name-48")
        assertEquals(RedeliveryEstimateStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newRedeliveryEstimate("code-240", "name-48")
        try {
            item.moveTo(RedeliveryEstimateStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RedeliveryEstimateStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newRedeliveryEstimate("code-240", "name-48")
        item.status = RedeliveryEstimateStatus.CANCELLED
        assertFails("move") { item.moveTo(RedeliveryEstimateStatus.PLANNED) }
    }
}
