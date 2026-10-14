package com.acme.transport.linehaul.relay.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RelayPlanTest {
    @Test
    fun `starts in new`() {
        val item = newRelayPlan("code-438", "title-93")
        assertEquals(RelayPlanStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to failed`() {
        val item = newRelayPlan("code-438", "title-93")
        try {
            item.moveTo(RelayPlanStatus.FAILED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RelayPlanStatus.FAILED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRelayPlan("code-438", "title-93")
        item.status = RelayPlanStatus.FAILED
        assertFails("move") { item.moveTo(RelayPlanStatus.NEW) }
    }
}
