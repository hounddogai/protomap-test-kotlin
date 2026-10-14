package com.acme.transport.customs.tariff.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualTariffVarianceTest {
    @Test
    fun `starts in draft`() {
        val item = newManualTariffVariance("code-85", "name-308")
        assertEquals(ManualTariffVarianceStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to closed`() {
        val item = newManualTariffVariance("code-85", "name-308")
        try {
            item.moveTo(ManualTariffVarianceStatus.CLOSED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualTariffVarianceStatus.CLOSED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newManualTariffVariance("code-85", "name-308")
        item.status = ManualTariffVarianceStatus.CLOSED
        assertFails("move") { item.moveTo(ManualTariffVarianceStatus.DRAFT) }
    }
}
