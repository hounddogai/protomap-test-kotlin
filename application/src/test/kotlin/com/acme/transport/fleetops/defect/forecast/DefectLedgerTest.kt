package com.acme.transport.fleetops.defect.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefectLedgerTest {
    @Test
    fun `starts in requested`() {
        val item = newDefectLedger("code-244", "title-224")
        assertEquals(DefectLedgerStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newDefectLedger("code-244", "title-224")
        try {
            item.moveTo(DefectLedgerStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefectLedgerStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newDefectLedger("code-244", "title-224")
        item.status = DefectLedgerStatus.EXPIRED
        assertFails("move") { item.moveTo(DefectLedgerStatus.REQUESTED) }
    }
}
