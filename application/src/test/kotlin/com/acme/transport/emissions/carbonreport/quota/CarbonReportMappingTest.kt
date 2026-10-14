package com.acme.transport.emissions.carbonreport.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CarbonReportMappingTest {
    @Test
    fun `starts in requested`() {
        val item = newCarbonReportMapping("code-267", "title-759")
        assertEquals(CarbonReportMappingStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newCarbonReportMapping("code-267", "title-759")
        try {
            item.moveTo(CarbonReportMappingStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CarbonReportMappingStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newCarbonReportMapping("code-267", "title-759")
        item.status = CarbonReportMappingStatus.EXPIRED
        assertFails("move") { item.moveTo(CarbonReportMappingStatus.REQUESTED) }
    }
}
