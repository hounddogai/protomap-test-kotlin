package com.acme.transport.fleetops.serviceplan.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualServicePlanRequestTest {
    @Test
    fun `starts in created`() {
        val item = newManualServicePlanRequest("code-881", "title-405")
        assertEquals(ManualServicePlanRequestStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newManualServicePlanRequest("code-881", "title-405")
        try {
            item.moveTo(ManualServicePlanRequestStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualServicePlanRequestStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newManualServicePlanRequest("code-881", "title-405")
        item.status = ManualServicePlanRequestStatus.RETIRED
        assertFails("move") { item.moveTo(ManualServicePlanRequestStatus.CREATED) }
    }
}
