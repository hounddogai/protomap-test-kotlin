package com.acme.transport.customs.tariff.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultTariffEstimateTest {
    @Test
    fun `starts in open`() {
        val item = newDefaultTariffEstimate("code-179", "name-618")
        assertEquals(DefaultTariffEstimateStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newDefaultTariffEstimate("code-179", "name-618")
        try {
            item.moveTo(DefaultTariffEstimateStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultTariffEstimateStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newDefaultTariffEstimate("code-179", "name-618")
        item.status = DefaultTariffEstimateStatus.ARCHIVED
        assertFails("move") { item.moveTo(DefaultTariffEstimateStatus.OPEN) }
    }
}
