package com.acme.transport.freightpay.accessorial.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingAccessorialEstimateTest {
    @Test
    fun `starts in idle`() {
        val item = newRollingAccessorialEstimate("code-568", "title-245")
        assertEquals(RollingAccessorialEstimateStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newRollingAccessorialEstimate("code-568", "title-245")
        try {
            item.moveTo(RollingAccessorialEstimateStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingAccessorialEstimateStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newRollingAccessorialEstimate("code-568", "title-245")
        item.status = RollingAccessorialEstimateStatus.STOPPED
        assertFails("move") { item.moveTo(RollingAccessorialEstimateStatus.IDLE) }
    }
}
