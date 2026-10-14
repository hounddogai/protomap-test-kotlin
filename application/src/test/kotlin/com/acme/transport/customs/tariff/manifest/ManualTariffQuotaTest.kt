package com.acme.transport.customs.tariff.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualTariffQuotaTest {
    @Test
    fun `starts in created`() {
        val item = newManualTariffQuota("code-974", "title-474")
        assertEquals(ManualTariffQuotaStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newManualTariffQuota("code-974", "title-474")
        try {
            item.moveTo(ManualTariffQuotaStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualTariffQuotaStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newManualTariffQuota("code-974", "title-474")
        item.status = ManualTariffQuotaStatus.RETIRED
        assertFails("move") { item.moveTo(ManualTariffQuotaStatus.CREATED) }
    }
}
