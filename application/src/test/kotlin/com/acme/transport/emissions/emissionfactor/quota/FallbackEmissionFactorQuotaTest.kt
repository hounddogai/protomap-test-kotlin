package com.acme.transport.emissions.emissionfactor.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackEmissionFactorQuotaTest {
    @Test
    fun `starts in idle`() {
        val item = newFallbackEmissionFactorQuota("code-917", "name-596")
        assertEquals(FallbackEmissionFactorQuotaStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newFallbackEmissionFactorQuota("code-917", "name-596")
        try {
            item.moveTo(FallbackEmissionFactorQuotaStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackEmissionFactorQuotaStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newFallbackEmissionFactorQuota("code-917", "name-596")
        item.status = FallbackEmissionFactorQuotaStatus.STOPPED
        assertFails("move") { item.moveTo(FallbackEmissionFactorQuotaStatus.IDLE) }
    }
}
