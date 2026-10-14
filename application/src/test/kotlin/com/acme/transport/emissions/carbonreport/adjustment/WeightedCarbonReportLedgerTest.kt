package com.acme.transport.emissions.carbonreport.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedCarbonReportLedgerTest {
    @Test
    fun `starts in new`() {
        val item = newWeightedCarbonReportLedger("code-53", "name-547")
        assertEquals(WeightedCarbonReportLedgerStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newWeightedCarbonReportLedger("code-53", "name-547")
        try {
            item.moveTo(WeightedCarbonReportLedgerStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedCarbonReportLedgerStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newWeightedCarbonReportLedger("code-53", "name-547")
        item.status = WeightedCarbonReportLedgerStatus.FAILED
        assertFails("move") { item.moveTo(WeightedCarbonReportLedgerStatus.NEW) }
    }
}
