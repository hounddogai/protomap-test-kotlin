package com.acme.transport.safety.coaching.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CoachingTemplateTest {
    @Test
    fun `starts in idle`() {
        val item = newCoachingTemplate("code-586", "label-148")
        assertEquals(CoachingTemplateStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newCoachingTemplate("code-586", "label-148")
        try {
            item.moveTo(CoachingTemplateStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CoachingTemplateStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newCoachingTemplate("code-586", "label-148")
        item.status = CoachingTemplateStatus.STOPPED
        assertFails("move") { item.moveTo(CoachingTemplateStatus.IDLE) }
    }
}
