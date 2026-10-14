package com.acme.transport.fleetops.inspection.globalinspectionscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyInspectionLedgerTest {
    @Test
    fun `starts in draft`() {
        val item = newLegacyInspectionLedger("code-771", "title-549")
        assertEquals(LegacyInspectionLedgerStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newLegacyInspectionLedger("code-771", "title-549")
        try {
            item.moveTo(LegacyInspectionLedgerStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyInspectionLedgerStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newLegacyInspectionLedger("code-771", "title-549")
        item.status = LegacyInspectionLedgerStatus.CLOSED
        assertFails("move") { item.moveTo(LegacyInspectionLedgerStatus.DRAFT) }
    }
}
