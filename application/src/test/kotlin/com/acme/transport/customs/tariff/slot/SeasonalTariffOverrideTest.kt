package com.acme.transport.customs.tariff.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalTariffOverrideTest {
    @Test
    fun `starts in open`() {
        val item = newSeasonalTariffOverride("code-903", "name-823")
        assertEquals(SeasonalTariffOverrideStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newSeasonalTariffOverride("code-903", "name-823")
        try {
            item.moveTo(SeasonalTariffOverrideStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalTariffOverrideStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newSeasonalTariffOverride("code-903", "name-823")
        item.status = SeasonalTariffOverrideStatus.ARCHIVED
        assertFails("move") { item.moveTo(SeasonalTariffOverrideStatus.OPEN) }
    }
}
