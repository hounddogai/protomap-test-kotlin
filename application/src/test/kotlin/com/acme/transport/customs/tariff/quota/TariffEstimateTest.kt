package com.acme.transport.customs.tariff.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TariffEstimateTest {
    @Test
    fun `starts in planned`() {
        val item = newTariffEstimate("code-803", "name-943")
        assertEquals(TariffEstimateStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newTariffEstimate("code-803", "name-943")
        try {
            item.moveTo(TariffEstimateStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TariffEstimateStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newTariffEstimate("code-803", "name-943")
        item.status = TariffEstimateStatus.CANCELLED
        assertFails("move") { item.moveTo(TariffEstimateStatus.PLANNED) }
    }
}
