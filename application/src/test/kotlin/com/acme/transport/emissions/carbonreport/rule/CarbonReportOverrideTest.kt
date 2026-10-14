package com.acme.transport.emissions.carbonreport.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CarbonReportOverrideTest {
    @Test
    fun `starts in new`() {
        val item = newCarbonReportOverride("code-444", "name-601")
        assertEquals(CarbonReportOverrideStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newCarbonReportOverride("code-444", "name-601")
        try {
            item.moveTo(CarbonReportOverrideStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CarbonReportOverrideStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newCarbonReportOverride("code-444", "name-601")
        item.status = CarbonReportOverrideStatus.FAILED
        assertFails("move") { item.moveTo(CarbonReportOverrideStatus.NEW) }
    }
}
