package com.acme.transport.emissions.emissionfactor.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalEmissionFactorQuotaTest {
    @Test
    fun `starts in idle`() {
        val item = newSeasonalEmissionFactorQuota("code-983", "name-350")
        assertEquals(SeasonalEmissionFactorQuotaStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newSeasonalEmissionFactorQuota("code-983", "name-350")
        try {
            item.moveTo(SeasonalEmissionFactorQuotaStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalEmissionFactorQuotaStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newSeasonalEmissionFactorQuota("code-983", "name-350")
        item.status = SeasonalEmissionFactorQuotaStatus.STOPPED
        assertFails("move") { item.moveTo(SeasonalEmissionFactorQuotaStatus.IDLE) }
    }
}
