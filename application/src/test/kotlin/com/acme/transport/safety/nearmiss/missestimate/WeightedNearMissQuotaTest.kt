package com.acme.transport.safety.nearmiss.missestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedNearMissQuotaTest {
    @Test
    fun `starts in draft`() {
        val item = newWeightedNearMissQuota("code-158", "name-244")
        assertEquals(WeightedNearMissQuotaStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newWeightedNearMissQuota("code-158", "name-244")
        try {
            item.moveTo(WeightedNearMissQuotaStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedNearMissQuotaStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newWeightedNearMissQuota("code-158", "name-244")
        item.status = WeightedNearMissQuotaStatus.CLOSED
        assertFails("move") { item.moveTo(WeightedNearMissQuotaStatus.DRAFT) }
    }
}
