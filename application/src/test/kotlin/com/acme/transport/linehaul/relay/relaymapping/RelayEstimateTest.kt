package com.acme.transport.linehaul.relay.relaymapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RelayEstimateTest {
    @Test
    fun `starts in open`() {
        val item = newRelayEstimate("code-248", "name-558")
        assertEquals(RelayEstimateStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newRelayEstimate("code-248", "name-558")
        try {
            item.moveTo(RelayEstimateStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RelayEstimateStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newRelayEstimate("code-248", "name-558")
        item.status = RelayEstimateStatus.ARCHIVED
        assertFails("move") { item.moveTo(RelayEstimateStatus.OPEN) }
    }
}
