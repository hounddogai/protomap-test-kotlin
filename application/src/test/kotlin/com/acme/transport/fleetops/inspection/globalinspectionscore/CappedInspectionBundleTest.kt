package com.acme.transport.fleetops.inspection.globalinspectionscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedInspectionBundleTest {
    @Test
    fun `starts in open`() {
        val item = newCappedInspectionBundle("code-761", "label-388")
        assertEquals(CappedInspectionBundleStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newCappedInspectionBundle("code-761", "label-388")
        try {
            item.moveTo(CappedInspectionBundleStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedInspectionBundleStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newCappedInspectionBundle("code-761", "label-388")
        item.status = CappedInspectionBundleStatus.ARCHIVED
        assertFails("move") { item.moveTo(CappedInspectionBundleStatus.OPEN) }
    }
}
