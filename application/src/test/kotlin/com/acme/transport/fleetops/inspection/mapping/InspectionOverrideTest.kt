package com.acme.transport.fleetops.inspection.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionOverrideTest {
    @Test
    fun `starts in draft`() {
        val item = newInspectionOverride("code-778", "name-226")
        assertEquals(InspectionOverrideStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newInspectionOverride("code-778", "name-226")
        try {
            item.moveTo(InspectionOverrideStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionOverrideStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newInspectionOverride("code-778", "name-226")
        item.status = InspectionOverrideStatus.CLOSED
        assertFails("move") { item.moveTo(InspectionOverrideStatus.DRAFT) }
    }
}
