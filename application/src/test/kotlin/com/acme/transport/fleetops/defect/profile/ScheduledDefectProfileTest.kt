package com.acme.transport.fleetops.defect.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledDefectProfileTest {
    @Test
    fun `starts in new`() {
        val item = newScheduledDefectProfile("code-128", "name-384")
        assertEquals(ScheduledDefectProfileStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newScheduledDefectProfile("code-128", "name-384")
        try {
            item.moveTo(ScheduledDefectProfileStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledDefectProfileStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newScheduledDefectProfile("code-128", "name-384")
        item.status = ScheduledDefectProfileStatus.FAILED
        assertFails("move") { item.moveTo(ScheduledDefectProfileStatus.NEW) }
    }
}
