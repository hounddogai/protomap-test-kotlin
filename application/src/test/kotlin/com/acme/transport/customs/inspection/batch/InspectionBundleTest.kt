package com.acme.transport.customs.inspection.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionBundleTest {
    @Test
    fun `starts in draft`() {
        val item = newInspectionBundle("code-595", "title-128")
        assertEquals(InspectionBundleStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newInspectionBundle("code-595", "title-128")
        try {
            item.moveTo(InspectionBundleStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionBundleStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newInspectionBundle("code-595", "title-128")
        item.status = InspectionBundleStatus.CLOSED
        assertFails("move") { item.moveTo(InspectionBundleStatus.DRAFT) }
    }
}
