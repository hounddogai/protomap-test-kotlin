package com.acme.transport.network.hub.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class HubScoreTest {
    @Test
    fun `starts in open`() {
        val item = newHubScore("code-937", "label-336")
        assertEquals(HubScoreStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newHubScore("code-937", "label-336")
        try {
            item.moveTo(HubScoreStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(HubScoreStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newHubScore("code-937", "label-336")
        item.status = HubScoreStatus.ARCHIVED
        assertFails("move") { item.moveTo(HubScoreStatus.OPEN) }
    }
}
