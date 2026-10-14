package com.acme.transport.network.transittime.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TransitTimeQuotaTest {
    @Test
    fun `starts in draft`() {
        val item = newTransitTimeQuota("code-208", "title-917")
        assertEquals(TransitTimeQuotaStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newTransitTimeQuota("code-208", "title-917")
        try {
            item.moveTo(TransitTimeQuotaStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TransitTimeQuotaStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newTransitTimeQuota("code-208", "title-917")
        item.status = TransitTimeQuotaStatus.CLOSED
        assertFails("move") { item.moveTo(TransitTimeQuotaStatus.DRAFT) }
    }
}
