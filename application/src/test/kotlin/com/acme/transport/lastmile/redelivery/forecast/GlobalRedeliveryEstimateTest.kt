package com.acme.transport.lastmile.redelivery.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalRedeliveryEstimateTest {
    @Test
    fun `starts in idle`() {
        val item = newGlobalRedeliveryEstimate("code-483", "title-76")
        assertEquals(GlobalRedeliveryEstimateStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newGlobalRedeliveryEstimate("code-483", "title-76")
        try {
            item.moveTo(GlobalRedeliveryEstimateStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalRedeliveryEstimateStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newGlobalRedeliveryEstimate("code-483", "title-76")
        item.status = GlobalRedeliveryEstimateStatus.STOPPED
        assertFails("move") { item.moveTo(GlobalRedeliveryEstimateStatus.IDLE) }
    }
}
