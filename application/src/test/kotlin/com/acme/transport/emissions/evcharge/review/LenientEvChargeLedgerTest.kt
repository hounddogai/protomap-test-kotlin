package com.acme.transport.emissions.evcharge.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientEvChargeLedgerTest {
    @Test
    fun `starts in open`() {
        val item = newLenientEvChargeLedger("code-275", "label-26")
        assertEquals(LenientEvChargeLedgerStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newLenientEvChargeLedger("code-275", "label-26")
        try {
            item.moveTo(LenientEvChargeLedgerStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientEvChargeLedgerStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newLenientEvChargeLedger("code-275", "label-26")
        item.status = LenientEvChargeLedgerStatus.ARCHIVED
        assertFails("move") { item.moveTo(LenientEvChargeLedgerStatus.OPEN) }
    }
}
