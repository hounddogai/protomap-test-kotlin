package com.acme.transport.fleetops.tire.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TireQuotaTest {
    @Test
    fun `starts in idle`() {
        val item = newTireQuota("code-315", "label-925")
        assertEquals(TireQuotaStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newTireQuota("code-315", "label-925")
        try {
            item.moveTo(TireQuotaStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TireQuotaStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newTireQuota("code-315", "label-925")
        item.status = TireQuotaStatus.STOPPED
        assertFails("move") { item.moveTo(TireQuotaStatus.IDLE) }
    }
}
