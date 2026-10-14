package com.acme.transport.customs.inspection.inspectionrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionTemplateTest {
    @Test
    fun `starts in created`() {
        val item = newInspectionTemplate("code-435", "name-893")
        assertEquals(InspectionTemplateStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newInspectionTemplate("code-435", "name-893")
        try {
            item.moveTo(InspectionTemplateStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionTemplateStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newInspectionTemplate("code-435", "name-893")
        item.status = InspectionTemplateStatus.RETIRED
        assertFails("move") { item.moveTo(InspectionTemplateStatus.CREATED) }
    }
}
