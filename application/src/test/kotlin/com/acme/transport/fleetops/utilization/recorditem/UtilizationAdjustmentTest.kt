package com.acme.transport.fleetops.utilization.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class UtilizationAdjustmentTest {
    @Test
    fun `starts in new`() {
        val item = newUtilizationAdjustment("code-680", "name-786")
        assertEquals(UtilizationAdjustmentStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newUtilizationAdjustment("code-680", "name-786")
        try {
            item.moveTo(UtilizationAdjustmentStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(UtilizationAdjustmentStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newUtilizationAdjustment("code-680", "name-786")
        item.status = UtilizationAdjustmentStatus.FAILED
        assertFails("move") { item.moveTo(UtilizationAdjustmentStatus.NEW) }
    }
}
