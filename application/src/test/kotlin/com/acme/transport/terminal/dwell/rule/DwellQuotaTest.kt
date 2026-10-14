package com.acme.transport.terminal.dwell.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DwellQuotaTest {
    @Test
    fun `starts in idle`() {
        val item = newDwellQuota("code-869", "label-315")
        assertEquals(DwellQuotaStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newDwellQuota("code-869", "label-315")
        try {
            item.moveTo(DwellQuotaStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DwellQuotaStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newDwellQuota("code-869", "label-315")
        item.status = DwellQuotaStatus.STOPPED
        assertFails("move") { item.moveTo(DwellQuotaStatus.IDLE) }
    }
}
