package com.acme.transport.fleetops.inspection.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionBundleTest {
    @Test
    fun `starts in open`() {
        val item = newInspectionBundle("code-630", "name-474")
        assertEquals(InspectionBundleStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newInspectionBundle("code-630", "name-474")
        try {
            item.moveTo(InspectionBundleStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionBundleStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newInspectionBundle("code-630", "name-474")
        item.status = InspectionBundleStatus.ARCHIVED
        assertFails("move") { item.moveTo(InspectionBundleStatus.OPEN) }
    }
}
