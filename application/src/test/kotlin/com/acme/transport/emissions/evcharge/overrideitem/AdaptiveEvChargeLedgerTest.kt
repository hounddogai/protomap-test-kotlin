package com.acme.transport.emissions.evcharge.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveEvChargeLedgerTest {
    @Test
    fun `starts in requested`() {
        val item = newAdaptiveEvChargeLedger("code-453", "title-789")
        assertEquals(AdaptiveEvChargeLedgerStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newAdaptiveEvChargeLedger("code-453", "title-789")
        try {
            item.moveTo(AdaptiveEvChargeLedgerStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveEvChargeLedgerStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newAdaptiveEvChargeLedger("code-453", "title-789")
        item.status = AdaptiveEvChargeLedgerStatus.EXPIRED
        assertFails("move") { item.moveTo(AdaptiveEvChargeLedgerStatus.REQUESTED) }
    }
}
