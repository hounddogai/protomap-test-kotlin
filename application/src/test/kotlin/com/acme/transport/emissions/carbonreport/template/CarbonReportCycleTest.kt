package com.acme.transport.emissions.carbonreport.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CarbonReportCycleTest {
    @Test
    fun `starts in requested`() {
        val item = newCarbonReportCycle("code-608", "label-985")
        assertEquals(CarbonReportCycleStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newCarbonReportCycle("code-608", "label-985")
        try {
            item.moveTo(CarbonReportCycleStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CarbonReportCycleStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newCarbonReportCycle("code-608", "label-985")
        item.status = CarbonReportCycleStatus.EXPIRED
        assertFails("move") { item.moveTo(CarbonReportCycleStatus.REQUESTED) }
    }
}
