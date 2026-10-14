package com.acme.transport.network.hub.hubplan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientHubPlanTest {
    @Test
    fun `starts in proposed`() {
        val item = newLenientHubPlan("code-362", "title-569")
        assertEquals(LenientHubPlanStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newLenientHubPlan("code-362", "title-569")
        try {
            item.moveTo(LenientHubPlanStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientHubPlanStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newLenientHubPlan("code-362", "title-569")
        item.status = LenientHubPlanStatus.WITHDRAWN
        assertFails("move") { item.moveTo(LenientHubPlanStatus.PROPOSED) }
    }
}
