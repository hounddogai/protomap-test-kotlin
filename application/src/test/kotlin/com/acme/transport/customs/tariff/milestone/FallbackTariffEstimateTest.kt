package com.acme.transport.customs.tariff.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackTariffEstimateTest {
    @Test
    fun `starts in new`() {
        val item = newFallbackTariffEstimate("code-951", "title-621")
        assertEquals(FallbackTariffEstimateStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newFallbackTariffEstimate("code-951", "title-621")
        try {
            item.moveTo(FallbackTariffEstimateStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackTariffEstimateStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newFallbackTariffEstimate("code-951", "title-621")
        item.status = FallbackTariffEstimateStatus.FAILED
        assertFails("move") { item.moveTo(FallbackTariffEstimateStatus.NEW) }
    }
}
