package com.acme.transport.linehaul.laneplan.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LanePlanTargetTest {
    @Test
    fun `starts in draft`() {
        val item = newLanePlanTarget("code-790", "title-45")
        assertEquals(LanePlanTargetStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newLanePlanTarget("code-790", "title-45")
        try {
            item.moveTo(LanePlanTargetStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LanePlanTargetStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newLanePlanTarget("code-790", "title-45")
        item.status = LanePlanTargetStatus.CLOSED
        assertFails("move") { item.moveTo(LanePlanTargetStatus.DRAFT) }
    }
}
