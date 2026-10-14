package com.acme.transport.fleetops.inspection.inspectionmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionLedgerTest {
    @Test
    fun `starts in open`() {
        val item = newInspectionLedger("code-478", "title-185")
        assertEquals(InspectionLedgerStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newInspectionLedger("code-478", "title-185")
        try {
            item.moveTo(InspectionLedgerStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionLedgerStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newInspectionLedger("code-478", "title-185")
        item.status = InspectionLedgerStatus.ARCHIVED
        assertFails("move") { item.moveTo(InspectionLedgerStatus.OPEN) }
    }
}
