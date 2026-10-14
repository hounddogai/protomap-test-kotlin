package com.acme.transport.emissions.carbonreport.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CarbonReportDigestTest {
    @Test
    fun `starts in idle`() {
        val item = newCarbonReportDigest("code-796", "title-196")
        assertEquals(CarbonReportDigestStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newCarbonReportDigest("code-796", "title-196")
        try {
            item.moveTo(CarbonReportDigestStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CarbonReportDigestStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newCarbonReportDigest("code-796", "title-196")
        item.status = CarbonReportDigestStatus.STOPPED
        assertFails("move") { item.moveTo(CarbonReportDigestStatus.IDLE) }
    }
}
