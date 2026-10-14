package com.acme.transport.linehaul.laneplan.planthreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingLanePlanDraftTest {
    @Test
    fun `starts in idle`() {
        val item = newRollingLanePlanDraft("code-364", "name-819")
        assertEquals(RollingLanePlanDraftStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newRollingLanePlanDraft("code-364", "name-819")
        try {
            item.moveTo(RollingLanePlanDraftStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingLanePlanDraftStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newRollingLanePlanDraft("code-364", "name-819")
        item.status = RollingLanePlanDraftStatus.STOPPED
        assertFails("move") { item.moveTo(RollingLanePlanDraftStatus.IDLE) }
    }
}
