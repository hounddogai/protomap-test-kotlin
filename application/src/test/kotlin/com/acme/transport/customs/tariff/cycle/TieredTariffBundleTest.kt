package com.acme.transport.customs.tariff.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredTariffBundleTest {
    @Test
    fun `starts in created`() {
        val item = newTieredTariffBundle("code-425", "name-827")
        assertEquals(TieredTariffBundleStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newTieredTariffBundle("code-425", "name-827")
        try {
            item.moveTo(TieredTariffBundleStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredTariffBundleStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newTieredTariffBundle("code-425", "name-827")
        item.status = TieredTariffBundleStatus.RETIRED
        assertFails("move") { item.moveTo(TieredTariffBundleStatus.CREATED) }
    }
}
