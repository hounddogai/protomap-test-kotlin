package com.acme.transport.linehaul.relay.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RelayMappingTest {
    @Test
    fun `starts in planned`() {
        val item = newRelayMapping("code-781", "label-927")
        assertEquals(RelayMappingStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newRelayMapping("code-781", "label-927")
        try {
            item.moveTo(RelayMappingStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RelayMappingStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newRelayMapping("code-781", "label-927")
        item.status = RelayMappingStatus.CANCELLED
        assertFails("move") { item.moveTo(RelayMappingStatus.PLANNED) }
    }
}
