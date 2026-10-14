package com.acme.transport.emissions.carbonreport.reportagreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientCarbonReportManifestTest {
    @Test
    fun `starts in draft`() {
        val item = newLenientCarbonReportManifest("code-794", "label-764")
        assertEquals(LenientCarbonReportManifestStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newLenientCarbonReportManifest("code-794", "label-764")
        try {
            item.moveTo(LenientCarbonReportManifestStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientCarbonReportManifestStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newLenientCarbonReportManifest("code-794", "label-764")
        item.status = LenientCarbonReportManifestStatus.CLOSED
        assertFails("move") { item.moveTo(LenientCarbonReportManifestStatus.DRAFT) }
    }
}
