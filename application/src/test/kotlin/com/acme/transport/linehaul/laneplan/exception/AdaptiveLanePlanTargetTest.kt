package com.acme.transport.linehaul.laneplan.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveLanePlanTargetTest {
    @Test
    fun `starts in planned`() {
        val item = newAdaptiveLanePlanTarget("code-252", "name-794")
        assertEquals(AdaptiveLanePlanTargetStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newAdaptiveLanePlanTarget("code-252", "name-794")
        try {
            item.moveTo(AdaptiveLanePlanTargetStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveLanePlanTargetStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newAdaptiveLanePlanTarget("code-252", "name-794")
        item.status = AdaptiveLanePlanTargetStatus.CANCELLED
        assertFails("move") { item.moveTo(AdaptiveLanePlanTargetStatus.PLANNED) }
    }
}
