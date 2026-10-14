package com.acme.transport.customs.inspection.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionTargetTest {
    @Test
    fun `starts in proposed`() {
        val item = newInspectionTarget("code-375", "title-707")
        assertEquals(InspectionTargetStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newInspectionTarget("code-375", "title-707")
        try {
            item.moveTo(InspectionTargetStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionTargetStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newInspectionTarget("code-375", "title-707")
        item.status = InspectionTargetStatus.WITHDRAWN
        assertFails("move") { item.moveTo(InspectionTargetStatus.PROPOSED) }
    }
}
