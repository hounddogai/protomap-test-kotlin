package com.acme.transport.customs.clearance.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ClearanceTemplateTest {
    @Test
    fun `starts in idle`() {
        val item = newClearanceTemplate("code-256", "title-71")
        assertEquals(ClearanceTemplateStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newClearanceTemplate("code-256", "title-71")
        try {
            item.moveTo(ClearanceTemplateStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ClearanceTemplateStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newClearanceTemplate("code-256", "title-71")
        item.status = ClearanceTemplateStatus.STOPPED
        assertFails("move") { item.moveTo(ClearanceTemplateStatus.IDLE) }
    }
}
