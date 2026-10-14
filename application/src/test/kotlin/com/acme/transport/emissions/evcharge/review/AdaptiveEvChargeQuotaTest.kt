package com.acme.transport.emissions.evcharge.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveEvChargeQuotaTest {
    @Test
    fun `starts in new`() {
        val item = newAdaptiveEvChargeQuota("code-949", "label-370")
        assertEquals(AdaptiveEvChargeQuotaStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newAdaptiveEvChargeQuota("code-949", "label-370")
        try {
            item.moveTo(AdaptiveEvChargeQuotaStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveEvChargeQuotaStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newAdaptiveEvChargeQuota("code-949", "label-370")
        item.status = AdaptiveEvChargeQuotaStatus.FAILED
        assertFails("move") { item.moveTo(AdaptiveEvChargeQuotaStatus.NEW) }
    }
}
