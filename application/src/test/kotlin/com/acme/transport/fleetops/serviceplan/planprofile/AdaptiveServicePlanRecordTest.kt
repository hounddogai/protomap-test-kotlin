package com.acme.transport.fleetops.serviceplan.planprofile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveServicePlanRecordTest {
    @Test
    fun `starts in draft`() {
        val item = newAdaptiveServicePlanRecord("code-29", "name-327")
        assertEquals(AdaptiveServicePlanRecordStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newAdaptiveServicePlanRecord("code-29", "name-327")
        try {
            item.moveTo(AdaptiveServicePlanRecordStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveServicePlanRecordStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newAdaptiveServicePlanRecord("code-29", "name-327")
        item.status = AdaptiveServicePlanRecordStatus.CLOSED
        assertFails("move") { item.moveTo(AdaptiveServicePlanRecordStatus.DRAFT) }
    }
}
