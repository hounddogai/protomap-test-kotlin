package com.acme.transport.crewing.qualification.qualificationassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledQualificationThresholdTest {
    @Test
    fun `starts in open`() {
        val item = newScheduledQualificationThreshold("code-426", "name-747")
        assertEquals(ScheduledQualificationThresholdStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newScheduledQualificationThreshold("code-426", "name-747")
        try {
            item.moveTo(ScheduledQualificationThresholdStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledQualificationThresholdStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newScheduledQualificationThreshold("code-426", "name-747")
        item.status = ScheduledQualificationThresholdStatus.ARCHIVED
        assertFails("move") { item.moveTo(ScheduledQualificationThresholdStatus.OPEN) }
    }
}
