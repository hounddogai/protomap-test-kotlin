package com.acme.transport.network.zone.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ZoneProposalTest {
    @Test
    fun `starts in planned`() {
        val item = newZoneProposal("code-665", "title-915")
        assertEquals(ZoneProposalStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newZoneProposal("code-665", "title-915")
        try {
            item.moveTo(ZoneProposalStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ZoneProposalStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newZoneProposal("code-665", "title-915")
        item.status = ZoneProposalStatus.CANCELLED
        assertFails("move") { item.moveTo(ZoneProposalStatus.PLANNED) }
    }
}
