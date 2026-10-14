package com.acme.transport.customs.tariff.tariffpolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalTariffRollupTest {
    @Test
    fun `starts in idle`() {
        val item = newRegionalTariffRollup("code-406", "name-17")
        assertEquals(RegionalTariffRollupStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newRegionalTariffRollup("code-406", "name-17")
        try {
            item.moveTo(RegionalTariffRollupStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalTariffRollupStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newRegionalTariffRollup("code-406", "name-17")
        item.status = RegionalTariffRollupStatus.STOPPED
        assertFails("move") { item.moveTo(RegionalTariffRollupStatus.IDLE) }
    }
}
