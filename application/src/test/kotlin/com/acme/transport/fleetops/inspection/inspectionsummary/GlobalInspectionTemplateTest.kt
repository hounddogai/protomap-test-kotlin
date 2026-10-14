package com.acme.transport.fleetops.inspection.inspectionsummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalInspectionTemplateTest {
    @Test
    fun `starts in planned`() {
        val item = newGlobalInspectionTemplate("code-77", "name-919")
        assertEquals(GlobalInspectionTemplateStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newGlobalInspectionTemplate("code-77", "name-919")
        try {
            item.moveTo(GlobalInspectionTemplateStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalInspectionTemplateStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newGlobalInspectionTemplate("code-77", "name-919")
        item.status = GlobalInspectionTemplateStatus.CANCELLED
        assertFails("move") { item.moveTo(GlobalInspectionTemplateStatus.PLANNED) }
    }
}
