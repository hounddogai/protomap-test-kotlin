package com.acme.transport.customs.inspection.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionLedgerTest {
    @Test
    fun `starts in created`() {
        val item = newInspectionLedger("code-163", "name-611")
        assertEquals(InspectionLedgerStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newInspectionLedger("code-163", "name-611")
        try {
            item.moveTo(InspectionLedgerStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionLedgerStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newInspectionLedger("code-163", "name-611")
        item.status = InspectionLedgerStatus.RETIRED
        assertFails("move") { item.moveTo(InspectionLedgerStatus.CREATED) }
    }
}
