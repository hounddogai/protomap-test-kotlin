package com.acme.transport.safety.cameraevent.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledCameraEventEstimateTest {
    @Test
    fun `starts in created`() {
        val item = newScheduledCameraEventEstimate("code-19", "name-571")
        assertEquals(ScheduledCameraEventEstimateStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newScheduledCameraEventEstimate("code-19", "name-571")
        try {
            item.moveTo(ScheduledCameraEventEstimateStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledCameraEventEstimateStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newScheduledCameraEventEstimate("code-19", "name-571")
        item.status = ScheduledCameraEventEstimateStatus.RETIRED
        assertFails("move") { item.moveTo(ScheduledCameraEventEstimateStatus.CREATED) }
    }
}
