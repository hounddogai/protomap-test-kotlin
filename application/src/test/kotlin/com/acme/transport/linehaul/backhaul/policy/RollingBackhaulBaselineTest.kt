package com.acme.transport.linehaul.backhaul.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingBackhaulBaselineTest {
    @Test
    fun `starts in idle`() {
        val item = newRollingBackhaulBaseline("code-926", "title-256")
        assertEquals(RollingBackhaulBaselineStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newRollingBackhaulBaseline("code-926", "title-256")
        try {
            item.moveTo(RollingBackhaulBaselineStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingBackhaulBaselineStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newRollingBackhaulBaseline("code-926", "title-256")
        item.status = RollingBackhaulBaselineStatus.STOPPED
        assertFails("move") { item.moveTo(RollingBackhaulBaselineStatus.IDLE) }
    }
}
