package com.acme.transport.network.hub.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingHubBaselineTest {
    @Test
    fun `starts in planned`() {
        val item = newRollingHubBaseline("code-958", "label-96")
        assertEquals(RollingHubBaselineStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newRollingHubBaseline("code-958", "label-96")
        try {
            item.moveTo(RollingHubBaselineStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingHubBaselineStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newRollingHubBaseline("code-958", "label-96")
        item.status = RollingHubBaselineStatus.CANCELLED
        assertFails("move") { item.moveTo(RollingHubBaselineStatus.PLANNED) }
    }
}
