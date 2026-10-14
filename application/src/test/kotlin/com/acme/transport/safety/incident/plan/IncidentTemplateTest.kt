package com.acme.transport.safety.incident.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class IncidentTemplateTest {
    @Test
    fun `starts in idle`() {
        val item = newIncidentTemplate("code-136", "title-873")
        assertEquals(IncidentTemplateStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newIncidentTemplate("code-136", "title-873")
        try {
            item.moveTo(IncidentTemplateStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(IncidentTemplateStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newIncidentTemplate("code-136", "title-873")
        item.status = IncidentTemplateStatus.STOPPED
        assertFails("move") { item.moveTo(IncidentTemplateStatus.IDLE) }
    }
}
