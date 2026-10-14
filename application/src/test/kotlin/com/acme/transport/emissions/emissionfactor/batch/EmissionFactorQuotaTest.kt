package com.acme.transport.emissions.emissionfactor.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class EmissionFactorQuotaTest {
    @Test
    fun `starts in draft`() {
        val item = newEmissionFactorQuota("code-696", "label-296")
        assertEquals(EmissionFactorQuotaStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newEmissionFactorQuota("code-696", "label-296")
        try {
            item.moveTo(EmissionFactorQuotaStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(EmissionFactorQuotaStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newEmissionFactorQuota("code-696", "label-296")
        item.status = EmissionFactorQuotaStatus.CLOSED
        assertFails("move") { item.moveTo(EmissionFactorQuotaStatus.DRAFT) }
    }
}
