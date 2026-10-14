package com.acme.transport.fleetops.inspection.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionRequestTest {
    @Test
    fun `starts in created`() {
        val item = newInspectionRequest("code-547", "title-378")
        assertEquals(InspectionRequestStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newInspectionRequest("code-547", "title-378")
        try {
            item.moveTo(InspectionRequestStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionRequestStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newInspectionRequest("code-547", "title-378")
        item.status = InspectionRequestStatus.RETIRED
        assertFails("move") { item.moveTo(InspectionRequestStatus.CREATED) }
    }
}
