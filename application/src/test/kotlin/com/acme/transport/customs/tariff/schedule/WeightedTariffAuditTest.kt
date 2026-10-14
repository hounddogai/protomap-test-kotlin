package com.acme.transport.customs.tariff.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedTariffAuditTest {
    @Test
    fun `starts in planned`() {
        val item = newWeightedTariffAudit("code-882", "label-37")
        assertEquals(WeightedTariffAuditStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newWeightedTariffAudit("code-882", "label-37")
        try {
            item.moveTo(WeightedTariffAuditStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedTariffAuditStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newWeightedTariffAudit("code-882", "label-37")
        item.status = WeightedTariffAuditStatus.CANCELLED
        assertFails("move") { item.moveTo(WeightedTariffAuditStatus.PLANNED) }
    }
}
