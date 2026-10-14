package com.acme.transport.emissions.evcharge.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedEvChargeQuotaTest {
    @Test
    fun `starts in proposed`() {
        val item = newWeightedEvChargeQuota("code-97", "label-474")
        assertEquals(WeightedEvChargeQuotaStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newWeightedEvChargeQuota("code-97", "label-474")
        try {
            item.moveTo(WeightedEvChargeQuotaStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedEvChargeQuotaStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newWeightedEvChargeQuota("code-97", "label-474")
        item.status = WeightedEvChargeQuotaStatus.WITHDRAWN
        assertFails("move") { item.moveTo(WeightedEvChargeQuotaStatus.PROPOSED) }
    }
}
