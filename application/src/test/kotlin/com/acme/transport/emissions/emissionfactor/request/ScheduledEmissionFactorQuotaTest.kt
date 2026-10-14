package com.acme.transport.emissions.emissionfactor.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledEmissionFactorQuotaTest {
    @Test
    fun `starts in requested`() {
        val item = newScheduledEmissionFactorQuota("code-513", "title-477")
        assertEquals(ScheduledEmissionFactorQuotaStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newScheduledEmissionFactorQuota("code-513", "title-477")
        try {
            item.moveTo(ScheduledEmissionFactorQuotaStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledEmissionFactorQuotaStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newScheduledEmissionFactorQuota("code-513", "title-477")
        item.status = ScheduledEmissionFactorQuotaStatus.EXPIRED
        assertFails("move") { item.moveTo(ScheduledEmissionFactorQuotaStatus.REQUESTED) }
    }
}
