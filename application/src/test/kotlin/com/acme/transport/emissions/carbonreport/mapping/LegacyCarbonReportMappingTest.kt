package com.acme.transport.emissions.carbonreport.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyCarbonReportMappingTest {
    @Test
    fun `starts in new`() {
        val item = newLegacyCarbonReportMapping("code-871", "title-497")
        assertEquals(LegacyCarbonReportMappingStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newLegacyCarbonReportMapping("code-871", "title-497")
        try {
            item.moveTo(LegacyCarbonReportMappingStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyCarbonReportMappingStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newLegacyCarbonReportMapping("code-871", "title-497")
        item.status = LegacyCarbonReportMappingStatus.FAILED
        assertFails("move") { item.moveTo(LegacyCarbonReportMappingStatus.NEW) }
    }
}
