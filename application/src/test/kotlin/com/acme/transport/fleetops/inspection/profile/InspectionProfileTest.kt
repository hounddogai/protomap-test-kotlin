package com.acme.transport.fleetops.inspection.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionProfileTest {
    @Test
    fun `starts in created`() {
        val item = newInspectionProfile("code-939", "title-162")
        assertEquals(InspectionProfileStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newInspectionProfile("code-939", "title-162")
        try {
            item.moveTo(InspectionProfileStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionProfileStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newInspectionProfile("code-939", "title-162")
        item.status = InspectionProfileStatus.RETIRED
        assertFails("move") { item.moveTo(InspectionProfileStatus.CREATED) }
    }
}
