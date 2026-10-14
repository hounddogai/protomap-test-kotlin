package com.acme.transport.linehaul.backhaul.backhauldigest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveBackhaulQuotaTest {
    @Test
    fun `starts in planned`() {
        val item = newAdaptiveBackhaulQuota("code-206", "name-636")
        assertEquals(AdaptiveBackhaulQuotaStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newAdaptiveBackhaulQuota("code-206", "name-636")
        try {
            item.moveTo(AdaptiveBackhaulQuotaStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveBackhaulQuotaStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newAdaptiveBackhaulQuota("code-206", "name-636")
        item.status = AdaptiveBackhaulQuotaStatus.CANCELLED
        assertFails("move") { item.moveTo(AdaptiveBackhaulQuotaStatus.PLANNED) }
    }
}
