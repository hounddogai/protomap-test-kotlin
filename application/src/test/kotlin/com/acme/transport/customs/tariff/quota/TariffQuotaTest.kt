package com.acme.transport.customs.tariff.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TariffQuotaTest {
    @Test
    fun `starts in idle`() {
        val item = newTariffQuota("code-170", "name-118")
        assertEquals(TariffQuotaStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newTariffQuota("code-170", "name-118")
        try {
            item.moveTo(TariffQuotaStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TariffQuotaStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newTariffQuota("code-170", "name-118")
        item.status = TariffQuotaStatus.STOPPED
        assertFails("move") { item.moveTo(TariffQuotaStatus.IDLE) }
    }
}
