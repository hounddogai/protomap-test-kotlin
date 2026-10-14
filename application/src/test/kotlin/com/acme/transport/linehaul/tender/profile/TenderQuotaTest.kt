package com.acme.transport.linehaul.tender.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TenderQuotaTest {
    @Test
    fun `starts in idle`() {
        val item = newTenderQuota("code-277", "label-439")
        assertEquals(TenderQuotaStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newTenderQuota("code-277", "label-439")
        try {
            item.moveTo(TenderQuotaStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TenderQuotaStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newTenderQuota("code-277", "label-439")
        item.status = TenderQuotaStatus.STOPPED
        assertFails("move") { item.moveTo(TenderQuotaStatus.IDLE) }
    }
}
