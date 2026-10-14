package com.acme.transport.network.node.nodeoverride

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveNodeWindowTest {
    @Test
    fun `starts in planned`() {
        val item = newAdaptiveNodeWindow("code-412", "label-246")
        assertEquals(AdaptiveNodeWindowStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newAdaptiveNodeWindow("code-412", "label-246")
        try {
            item.moveTo(AdaptiveNodeWindowStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveNodeWindowStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newAdaptiveNodeWindow("code-412", "label-246")
        item.status = AdaptiveNodeWindowStatus.CANCELLED
        assertFails("move") { item.moveTo(AdaptiveNodeWindowStatus.PLANNED) }
    }
}
