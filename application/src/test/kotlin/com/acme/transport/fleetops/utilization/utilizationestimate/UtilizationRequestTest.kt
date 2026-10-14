package com.acme.transport.fleetops.utilization.utilizationestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class UtilizationRequestTest {
    @Test
    fun `starts in planned`() {
        val item = newUtilizationRequest("code-931", "label-952")
        assertEquals(UtilizationRequestStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newUtilizationRequest("code-931", "label-952")
        try {
            item.moveTo(UtilizationRequestStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(UtilizationRequestStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newUtilizationRequest("code-931", "label-952")
        item.status = UtilizationRequestStatus.CANCELLED
        assertFails("move") { item.moveTo(UtilizationRequestStatus.PLANNED) }
    }
}
