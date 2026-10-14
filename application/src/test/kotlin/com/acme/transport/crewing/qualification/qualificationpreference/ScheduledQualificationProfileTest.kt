package com.acme.transport.crewing.qualification.qualificationpreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledQualificationProfileTest {
    @Test
    fun `starts in new`() {
        val item = newScheduledQualificationProfile("code-75", "title-40")
        assertEquals(ScheduledQualificationProfileStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newScheduledQualificationProfile("code-75", "title-40")
        try {
            item.moveTo(ScheduledQualificationProfileStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledQualificationProfileStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newScheduledQualificationProfile("code-75", "title-40")
        item.status = ScheduledQualificationProfileStatus.FAILED
        assertFails("move") { item.moveTo(ScheduledQualificationProfileStatus.NEW) }
    }
}
