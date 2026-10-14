package com.acme.transport.crewing.qualification.qualificationtemplate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledQualificationTemplateTest {
    @Test
    fun `starts in idle`() {
        val item = newScheduledQualificationTemplate("code-289", "label-947")
        assertEquals(ScheduledQualificationTemplateStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newScheduledQualificationTemplate("code-289", "label-947")
        try {
            item.moveTo(ScheduledQualificationTemplateStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledQualificationTemplateStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newScheduledQualificationTemplate("code-289", "label-947")
        item.status = ScheduledQualificationTemplateStatus.STOPPED
        assertFails("move") { item.moveTo(ScheduledQualificationTemplateStatus.IDLE) }
    }
}
