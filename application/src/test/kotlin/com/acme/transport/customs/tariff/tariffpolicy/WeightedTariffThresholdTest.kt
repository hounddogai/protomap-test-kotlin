package com.acme.transport.customs.tariff.tariffpolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedTariffThresholdTest {
    @Test
    fun `starts in planned`() {
        val item = newWeightedTariffThreshold("code-562", "label-255")
        assertEquals(WeightedTariffThresholdStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newWeightedTariffThreshold("code-562", "label-255")
        try {
            item.moveTo(WeightedTariffThresholdStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedTariffThresholdStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newWeightedTariffThreshold("code-562", "label-255")
        item.status = WeightedTariffThresholdStatus.CANCELLED
        assertFails("move") { item.moveTo(WeightedTariffThresholdStatus.PLANNED) }
    }
}
