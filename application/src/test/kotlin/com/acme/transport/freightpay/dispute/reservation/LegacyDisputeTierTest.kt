package com.acme.transport.freightpay.dispute.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyDisputeTierTest {
    @Test
    fun `starts in draft`() {
        val item = newLegacyDisputeTier("code-349", "title-201")
        assertEquals(LegacyDisputeTierStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newLegacyDisputeTier("code-349", "title-201")
        try {
            item.moveTo(LegacyDisputeTierStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyDisputeTierStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newLegacyDisputeTier("code-349", "title-201")
        item.status = LegacyDisputeTierStatus.CLOSED
        assertFails("move") { item.moveTo(LegacyDisputeTierStatus.DRAFT) }
    }
}
