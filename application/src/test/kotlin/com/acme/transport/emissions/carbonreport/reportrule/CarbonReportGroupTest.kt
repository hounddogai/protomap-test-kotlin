package com.acme.transport.emissions.carbonreport.reportrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CarbonReportGroupTest {
    @Test
    fun `starts in new`() {
        val item = newCarbonReportGroup("code-642", "label-376")
        assertEquals(CarbonReportGroupStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newCarbonReportGroup("code-642", "label-376")
        try {
            item.moveTo(CarbonReportGroupStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CarbonReportGroupStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newCarbonReportGroup("code-642", "label-376")
        item.status = CarbonReportGroupStatus.FAILED
        assertFails("move") { item.moveTo(CarbonReportGroupStatus.NEW) }
    }
}
