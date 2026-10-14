package com.acme.transport.linehaul.tender.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedTenderQuotaTest {
    @Test
    fun `starts in proposed`() {
        val item = newWeightedTenderQuota("code-51", "title-48")
        assertEquals(WeightedTenderQuotaStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newWeightedTenderQuota("code-51", "title-48")
        try {
            item.moveTo(WeightedTenderQuotaStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedTenderQuotaStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newWeightedTenderQuota("code-51", "title-48")
        item.status = WeightedTenderQuotaStatus.WITHDRAWN
        assertFails("move") { item.moveTo(WeightedTenderQuotaStatus.PROPOSED) }
    }
}
