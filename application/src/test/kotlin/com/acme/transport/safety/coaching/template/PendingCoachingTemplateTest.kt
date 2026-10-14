package com.acme.transport.safety.coaching.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingCoachingTemplateTest {
    @Test
    fun `starts in planned`() {
        val item = newPendingCoachingTemplate("code-987", "label-346")
        assertEquals(PendingCoachingTemplateStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newPendingCoachingTemplate("code-987", "label-346")
        try {
            item.moveTo(PendingCoachingTemplateStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingCoachingTemplateStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newPendingCoachingTemplate("code-987", "label-346")
        item.status = PendingCoachingTemplateStatus.CANCELLED
        assertFails("move") { item.moveTo(PendingCoachingTemplateStatus.PLANNED) }
    }
}
