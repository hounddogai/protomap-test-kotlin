package com.acme.transport.network.hub.legacyhubplan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientHubEstimateTest {
    @Test
    fun `starts in idle`() {
        val item = newLenientHubEstimate("code-140", "name-608")
        assertEquals(LenientHubEstimateStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newLenientHubEstimate("code-140", "name-608")
        try {
            item.moveTo(LenientHubEstimateStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientHubEstimateStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newLenientHubEstimate("code-140", "name-608")
        item.status = LenientHubEstimateStatus.STOPPED
        assertFails("move") { item.moveTo(LenientHubEstimateStatus.IDLE) }
    }
}
