package com.acme.transport.safety.violation.violationslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledViolationTemplateTest {
    @Test
    fun `starts in idle`() {
        val item = newScheduledViolationTemplate("code-469", "name-472")
        assertEquals(ScheduledViolationTemplateStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newScheduledViolationTemplate("code-469", "name-472")
        try {
            item.moveTo(ScheduledViolationTemplateStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledViolationTemplateStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newScheduledViolationTemplate("code-469", "name-472")
        item.status = ScheduledViolationTemplateStatus.STOPPED
        assertFails("move") { item.moveTo(ScheduledViolationTemplateStatus.IDLE) }
    }
}
