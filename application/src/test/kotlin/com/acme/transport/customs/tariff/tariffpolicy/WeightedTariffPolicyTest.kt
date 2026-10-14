package com.acme.transport.customs.tariff.tariffpolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedTariffPolicyTest {
    @Test
    fun `starts in new`() {
        val item = newWeightedTariffPolicy("code-496", "name-128")
        assertEquals(WeightedTariffPolicyStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to failed`() {
        val item = newWeightedTariffPolicy("code-496", "name-128")
        try {
            item.moveTo(WeightedTariffPolicyStatus.FAILED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedTariffPolicyStatus.FAILED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newWeightedTariffPolicy("code-496", "name-128")
        item.status = WeightedTariffPolicyStatus.FAILED
        assertFails("move") { item.moveTo(WeightedTariffPolicyStatus.NEW) }
    }
}
