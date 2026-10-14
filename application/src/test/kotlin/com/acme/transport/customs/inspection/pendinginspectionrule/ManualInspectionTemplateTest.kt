package com.acme.transport.customs.inspection.pendinginspectionrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualInspectionTemplateTest {
    @Test
    fun `starts in requested`() {
        val item = newManualInspectionTemplate("code-538", "title-6")
        assertEquals(ManualInspectionTemplateStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newManualInspectionTemplate("code-538", "title-6")
        try {
            item.moveTo(ManualInspectionTemplateStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualInspectionTemplateStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newManualInspectionTemplate("code-538", "title-6")
        item.status = ManualInspectionTemplateStatus.EXPIRED
        assertFails("move") { item.moveTo(ManualInspectionTemplateStatus.REQUESTED) }
    }
}
