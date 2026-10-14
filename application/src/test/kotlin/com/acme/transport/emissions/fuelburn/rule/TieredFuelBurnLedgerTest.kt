package com.acme.transport.emissions.fuelburn.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredFuelBurnLedgerTest {
    @Test
    fun `starts in open`() {
        val item = newTieredFuelBurnLedger("code-154", "label-699")
        assertEquals(TieredFuelBurnLedgerStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newTieredFuelBurnLedger("code-154", "label-699")
        try {
            item.moveTo(TieredFuelBurnLedgerStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredFuelBurnLedgerStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newTieredFuelBurnLedger("code-154", "label-699")
        item.status = TieredFuelBurnLedgerStatus.ARCHIVED
        assertFails("move") { item.moveTo(TieredFuelBurnLedgerStatus.OPEN) }
    }
}
