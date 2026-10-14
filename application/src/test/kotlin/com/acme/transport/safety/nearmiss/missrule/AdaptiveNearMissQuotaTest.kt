package com.acme.transport.safety.nearmiss.missrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveNearMissQuotaTest {
    @Test
    fun `starts in created`() {
        val item = newAdaptiveNearMissQuota("code-294", "label-612")
        assertEquals(AdaptiveNearMissQuotaStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newAdaptiveNearMissQuota("code-294", "label-612")
        try {
            item.moveTo(AdaptiveNearMissQuotaStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveNearMissQuotaStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newAdaptiveNearMissQuota("code-294", "label-612")
        item.status = AdaptiveNearMissQuotaStatus.RETIRED
        assertFails("move") { item.moveTo(AdaptiveNearMissQuotaStatus.CREATED) }
    }
}
