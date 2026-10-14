package com.acme.transport.network.cutoff.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalCutoffTargetTest {
    @Test
    fun `starts in proposed`() {
        val item = newRegionalCutoffTarget("code-381", "name-62")
        assertEquals(RegionalCutoffTargetStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newRegionalCutoffTarget("code-381", "name-62")
        try {
            item.moveTo(RegionalCutoffTargetStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalCutoffTargetStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newRegionalCutoffTarget("code-381", "name-62")
        item.status = RegionalCutoffTargetStatus.WITHDRAWN
        assertFails("move") { item.moveTo(RegionalCutoffTargetStatus.PROPOSED) }
    }
}
