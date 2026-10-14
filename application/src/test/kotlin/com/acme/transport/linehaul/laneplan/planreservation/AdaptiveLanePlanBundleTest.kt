package com.acme.transport.linehaul.laneplan.planreservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveLanePlanBundleTest {
    @Test
    fun `starts in proposed`() {
        val item = newAdaptiveLanePlanBundle("code-847", "label-973")
        assertEquals(AdaptiveLanePlanBundleStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newAdaptiveLanePlanBundle("code-847", "label-973")
        try {
            item.moveTo(AdaptiveLanePlanBundleStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveLanePlanBundleStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newAdaptiveLanePlanBundle("code-847", "label-973")
        item.status = AdaptiveLanePlanBundleStatus.WITHDRAWN
        assertFails("move") { item.moveTo(AdaptiveLanePlanBundleStatus.PROPOSED) }
    }
}
