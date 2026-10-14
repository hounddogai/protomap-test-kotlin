package com.acme.transport.customs.tariff.tariffoverride

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalTariffOverrideTest {
    @Test
    fun `starts in requested`() {
        val item = newRegionalTariffOverride("code-71", "name-63")
        assertEquals(RegionalTariffOverrideStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newRegionalTariffOverride("code-71", "name-63")
        try {
            item.moveTo(RegionalTariffOverrideStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalTariffOverrideStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRegionalTariffOverride("code-71", "name-63")
        item.status = RegionalTariffOverrideStatus.EXPIRED
        assertFails("move") { item.moveTo(RegionalTariffOverrideStatus.REQUESTED) }
    }
}
