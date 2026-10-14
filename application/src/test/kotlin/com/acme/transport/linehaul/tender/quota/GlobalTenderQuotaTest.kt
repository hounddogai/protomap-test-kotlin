package com.acme.transport.linehaul.tender.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalTenderQuotaTest {
    @Test
    fun `starts in open`() {
        val item = newGlobalTenderQuota("code-120", "label-665")
        assertEquals(GlobalTenderQuotaStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newGlobalTenderQuota("code-120", "label-665")
        try {
            item.moveTo(GlobalTenderQuotaStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalTenderQuotaStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newGlobalTenderQuota("code-120", "label-665")
        item.status = GlobalTenderQuotaStatus.ARCHIVED
        assertFails("move") { item.moveTo(GlobalTenderQuotaStatus.OPEN) }
    }
}
