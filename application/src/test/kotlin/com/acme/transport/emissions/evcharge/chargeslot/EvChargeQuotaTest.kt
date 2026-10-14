package com.acme.transport.emissions.evcharge.chargeslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class EvChargeQuotaTest {
    @Test
    fun `starts in open`() {
        val item = newEvChargeQuota("code-737", "title-423")
        assertEquals(EvChargeQuotaStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newEvChargeQuota("code-737", "title-423")
        try {
            item.moveTo(EvChargeQuotaStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(EvChargeQuotaStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newEvChargeQuota("code-737", "title-423")
        item.status = EvChargeQuotaStatus.ARCHIVED
        assertFails("move") { item.moveTo(EvChargeQuotaStatus.OPEN) }
    }
}
