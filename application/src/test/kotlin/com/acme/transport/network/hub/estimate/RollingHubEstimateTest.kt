package com.acme.transport.network.hub.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingHubEstimateTest {
    @Test
    fun `starts in requested`() {
        val item = newRollingHubEstimate("code-858", "label-22")
        assertEquals(RollingHubEstimateStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newRollingHubEstimate("code-858", "label-22")
        try {
            item.moveTo(RollingHubEstimateStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingHubEstimateStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRollingHubEstimate("code-858", "label-22")
        item.status = RollingHubEstimateStatus.EXPIRED
        assertFails("move") { item.moveTo(RollingHubEstimateStatus.REQUESTED) }
    }
}
