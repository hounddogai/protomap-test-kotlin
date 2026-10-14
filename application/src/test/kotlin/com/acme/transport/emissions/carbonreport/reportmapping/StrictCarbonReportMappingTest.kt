package com.acme.transport.emissions.carbonreport.reportmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictCarbonReportMappingTest {
    @Test
    fun `starts in open`() {
        val item = newStrictCarbonReportMapping("code-53", "label-684")
        assertEquals(StrictCarbonReportMappingStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newStrictCarbonReportMapping("code-53", "label-684")
        try {
            item.moveTo(StrictCarbonReportMappingStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictCarbonReportMappingStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newStrictCarbonReportMapping("code-53", "label-684")
        item.status = StrictCarbonReportMappingStatus.ARCHIVED
        assertFails("move") { item.moveTo(StrictCarbonReportMappingStatus.OPEN) }
    }
}
