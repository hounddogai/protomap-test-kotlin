package com.acme.transport.fleetops.inspection.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionTemplateTest {
    @Test
    fun `starts in requested`() {
        val item = newInspectionTemplate("code-70", "name-797")
        assertEquals(InspectionTemplateStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newInspectionTemplate("code-70", "name-797")
        try {
            item.moveTo(InspectionTemplateStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionTemplateStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newInspectionTemplate("code-70", "name-797")
        item.status = InspectionTemplateStatus.EXPIRED
        assertFails("move") { item.moveTo(InspectionTemplateStatus.REQUESTED) }
    }
}
