package com.acme.transport.fleetops.inspection.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionTargetTest {
    @Test
    fun `starts in requested`() {
        val item = newInspectionTarget("code-791", "title-435")
        assertEquals(InspectionTargetStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newInspectionTarget("code-791", "title-435")
        try {
            item.moveTo(InspectionTargetStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionTargetStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newInspectionTarget("code-791", "title-435")
        item.status = InspectionTargetStatus.EXPIRED
        assertFails("move") { item.moveTo(InspectionTargetStatus.REQUESTED) }
    }
}
