package com.acme.transport.terminal.crossdock.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalCrossDockQuotaTest {
    @Test
    fun `starts in proposed`() {
        val item = newRegionalCrossDockQuota("code-773", "label-382")
        assertEquals(RegionalCrossDockQuotaStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newRegionalCrossDockQuota("code-773", "label-382")
        try {
            item.moveTo(RegionalCrossDockQuotaStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalCrossDockQuotaStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newRegionalCrossDockQuota("code-773", "label-382")
        item.status = RegionalCrossDockQuotaStatus.WITHDRAWN
        assertFails("move") { item.moveTo(RegionalCrossDockQuotaStatus.PROPOSED) }
    }
}
