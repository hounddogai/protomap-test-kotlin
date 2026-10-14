package com.acme.transport.emissions.carbonreport.reportadjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalCarbonReportManifestTest {
    @Test
    fun `starts in new`() {
        val item = newRegionalCarbonReportManifest("code-182", "title-224")
        assertEquals(RegionalCarbonReportManifestStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newRegionalCarbonReportManifest("code-182", "title-224")
        try {
            item.moveTo(RegionalCarbonReportManifestStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalCarbonReportManifestStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRegionalCarbonReportManifest("code-182", "title-224")
        item.status = RegionalCarbonReportManifestStatus.FAILED
        assertFails("move") { item.moveTo(RegionalCarbonReportManifestStatus.NEW) }
    }
}
