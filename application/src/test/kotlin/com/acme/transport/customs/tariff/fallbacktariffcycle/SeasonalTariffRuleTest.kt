package com.acme.transport.customs.tariff.fallbacktariffcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalTariffRuleTest {
    @Test
    fun `starts in idle`() {
        val item = newSeasonalTariffRule("code-455", "title-576")
        assertEquals(SeasonalTariffRuleStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newSeasonalTariffRule("code-455", "title-576")
        try {
            item.moveTo(SeasonalTariffRuleStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalTariffRuleStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newSeasonalTariffRule("code-455", "title-576")
        item.status = SeasonalTariffRuleStatus.STOPPED
        assertFails("move") { item.moveTo(SeasonalTariffRuleStatus.IDLE) }
    }
}
