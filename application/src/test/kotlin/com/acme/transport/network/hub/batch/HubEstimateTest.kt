package com.acme.transport.network.hub.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class HubEstimateTest {
    @Test
    fun `starts in new`() {
        val item = newHubEstimate("code-28", "label-101")
        assertEquals(HubEstimateStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newHubEstimate("code-28", "label-101")
        try {
            item.moveTo(HubEstimateStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(HubEstimateStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newHubEstimate("code-28", "label-101")
        item.status = HubEstimateStatus.FAILED
        assertFails("move") { item.moveTo(HubEstimateStatus.NEW) }
    }
}
