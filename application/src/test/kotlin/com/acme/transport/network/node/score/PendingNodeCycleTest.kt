package com.acme.transport.network.node.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingNodeCycleTest {
    @Test
    fun `starts in created`() {
        val item = newPendingNodeCycle("code-274", "title-438")
        assertEquals(PendingNodeCycleStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newPendingNodeCycle("code-274", "title-438")
        try {
            item.moveTo(PendingNodeCycleStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingNodeCycleStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newPendingNodeCycle("code-274", "title-438")
        item.status = PendingNodeCycleStatus.RETIRED
        assertFails("move") { item.moveTo(PendingNodeCycleStatus.CREATED) }
    }
}
