package com.acme.transport.network.node.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class NodeReservationTest {
    @Test
    fun `starts in created`() {
        val item = newNodeReservation("code-741", "title-856")
        assertEquals(NodeReservationStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newNodeReservation("code-741", "title-856")
        try {
            item.moveTo(NodeReservationStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(NodeReservationStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newNodeReservation("code-741", "title-856")
        item.status = NodeReservationStatus.RETIRED
        assertFails("move") { item.moveTo(NodeReservationStatus.CREATED) }
    }
}
