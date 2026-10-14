package com.acme.transport.emissions.carbonreport.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredCarbonReportQuotaTest {
    @Test
    fun `starts in idle`() {
        val item = newTieredCarbonReportQuota("code-416", "label-671")
        assertEquals(TieredCarbonReportQuotaStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newTieredCarbonReportQuota("code-416", "label-671")
        try {
            item.moveTo(TieredCarbonReportQuotaStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredCarbonReportQuotaStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newTieredCarbonReportQuota("code-416", "label-671")
        item.status = TieredCarbonReportQuotaStatus.STOPPED
        assertFails("move") { item.moveTo(TieredCarbonReportQuotaStatus.IDLE) }
    }
}
