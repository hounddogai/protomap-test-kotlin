package com.acme.transport.customs.tariff.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TariffOverrideTest {
    @Test
    fun `starts in created`() {
        val item = newTariffOverride("code-151", "title-434")
        assertEquals(TariffOverrideStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newTariffOverride("code-151", "title-434")
        try {
            item.moveTo(TariffOverrideStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TariffOverrideStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newTariffOverride("code-151", "title-434")
        item.status = TariffOverrideStatus.RETIRED
        assertFails("move") { item.moveTo(TariffOverrideStatus.CREATED) }
    }
}
