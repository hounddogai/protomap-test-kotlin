package com.acme.transport.fleetops.defect.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualDefectLedgerTest {
    @Test
    fun `starts in open`() {
        val item = newManualDefectLedger("code-658", "label-742")
        assertEquals(ManualDefectLedgerStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newManualDefectLedger("code-658", "label-742")
        try {
            item.moveTo(ManualDefectLedgerStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualDefectLedgerStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newManualDefectLedger("code-658", "label-742")
        item.status = ManualDefectLedgerStatus.ARCHIVED
        assertFails("move") { item.moveTo(ManualDefectLedgerStatus.OPEN) }
    }
}
