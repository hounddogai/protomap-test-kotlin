package com.acme.transport.customs.tariff.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingTariffMetricTest {
    @Test
    fun `starts in requested`() {
        val item = newPendingTariffMetric("code-738", "name-407")
        assertEquals(PendingTariffMetricStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newPendingTariffMetric("code-738", "name-407")
        try {
            item.moveTo(PendingTariffMetricStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingTariffMetricStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newPendingTariffMetric("code-738", "name-407")
        item.status = PendingTariffMetricStatus.EXPIRED
        assertFails("move") { item.moveTo(PendingTariffMetricStatus.REQUESTED) }
    }
}
