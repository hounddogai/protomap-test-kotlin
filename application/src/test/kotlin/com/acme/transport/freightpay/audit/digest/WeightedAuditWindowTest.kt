package com.acme.transport.freightpay.audit.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedAuditWindowTest {
    @Test
    fun `starts in new`() {
        val item = newWeightedAuditWindow("code-918", "label-653")
        assertEquals(WeightedAuditWindowStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newWeightedAuditWindow("code-918", "label-653")
        try {
            item.moveTo(WeightedAuditWindowStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedAuditWindowStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newWeightedAuditWindow("code-918", "label-653")
        item.status = WeightedAuditWindowStatus.FAILED
        assertFails("move") { item.moveTo(WeightedAuditWindowStatus.NEW) }
    }
}
