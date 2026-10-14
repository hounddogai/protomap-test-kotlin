package com.acme.transport.terminal.crossdock.dockestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingCrossDockScoreTest {
    @Test
    fun `starts in planned`() {
        val item = newRollingCrossDockScore("code-127", "title-275")
        assertEquals(RollingCrossDockScoreStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newRollingCrossDockScore("code-127", "title-275")
        try {
            item.moveTo(RollingCrossDockScoreStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingCrossDockScoreStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newRollingCrossDockScore("code-127", "title-275")
        item.status = RollingCrossDockScoreStatus.CANCELLED
        assertFails("move") { item.moveTo(RollingCrossDockScoreStatus.PLANNED) }
    }
}
