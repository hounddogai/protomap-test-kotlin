package com.acme.transport.crewing.qualification.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class QualificationScheduleTest {
    @Test
    fun `starts in draft`() {
        val item = newQualificationSchedule("code-695", "label-183")
        assertEquals(QualificationScheduleStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newQualificationSchedule("code-695", "label-183")
        try {
            item.moveTo(QualificationScheduleStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(QualificationScheduleStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newQualificationSchedule("code-695", "label-183")
        item.status = QualificationScheduleStatus.CLOSED
        assertFails("move") { item.moveTo(QualificationScheduleStatus.DRAFT) }
    }
}
