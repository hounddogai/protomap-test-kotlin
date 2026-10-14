package com.acme.transport.linehaul.backhaul.regionalbackhaultarget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyBackhaulQuotaTest {
    @Test
    fun `starts in proposed`() {
        val item = newLegacyBackhaulQuota("code-546", "title-229")
        assertEquals(LegacyBackhaulQuotaStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newLegacyBackhaulQuota("code-546", "title-229")
        try {
            item.moveTo(LegacyBackhaulQuotaStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyBackhaulQuotaStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newLegacyBackhaulQuota("code-546", "title-229")
        item.status = LegacyBackhaulQuotaStatus.WITHDRAWN
        assertFails("move") { item.moveTo(LegacyBackhaulQuotaStatus.PROPOSED) }
    }
}
