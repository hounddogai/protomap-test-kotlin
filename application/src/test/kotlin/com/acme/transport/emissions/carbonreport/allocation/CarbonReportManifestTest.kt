package com.acme.transport.emissions.carbonreport.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CarbonReportManifestTest {
    @Test
    fun `starts in proposed`() {
        val item = newCarbonReportManifest("code-54", "title-595")
        assertEquals(CarbonReportManifestStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newCarbonReportManifest("code-54", "title-595")
        try {
            item.moveTo(CarbonReportManifestStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CarbonReportManifestStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newCarbonReportManifest("code-54", "title-595")
        item.status = CarbonReportManifestStatus.WITHDRAWN
        assertFails("move") { item.moveTo(CarbonReportManifestStatus.PROPOSED) }
    }
}
