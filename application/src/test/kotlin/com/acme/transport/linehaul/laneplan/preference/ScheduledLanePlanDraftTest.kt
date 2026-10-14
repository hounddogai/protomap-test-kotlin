package com.acme.transport.linehaul.laneplan.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledLanePlanDraftTest {
    @Test
    fun `starts in created`() {
        val item = newScheduledLanePlanDraft("code-298", "title-322")
        assertEquals(ScheduledLanePlanDraftStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newScheduledLanePlanDraft("code-298", "title-322")
        try {
            item.moveTo(ScheduledLanePlanDraftStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledLanePlanDraftStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newScheduledLanePlanDraft("code-298", "title-322")
        item.status = ScheduledLanePlanDraftStatus.RETIRED
        assertFails("move") { item.moveTo(ScheduledLanePlanDraftStatus.CREATED) }
    }
}
