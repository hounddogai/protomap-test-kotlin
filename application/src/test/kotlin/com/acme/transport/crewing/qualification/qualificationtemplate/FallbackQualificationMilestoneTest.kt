package com.acme.transport.crewing.qualification.qualificationtemplate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackQualificationMilestoneTest {
    @Test
    fun `starts in idle`() {
        val item = newFallbackQualificationMilestone("code-66", "title-183")
        assertEquals(FallbackQualificationMilestoneStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newFallbackQualificationMilestone("code-66", "title-183")
        try {
            item.moveTo(FallbackQualificationMilestoneStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackQualificationMilestoneStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newFallbackQualificationMilestone("code-66", "title-183")
        item.status = FallbackQualificationMilestoneStatus.STOPPED
        assertFails("move") { item.moveTo(FallbackQualificationMilestoneStatus.IDLE) }
    }
}
