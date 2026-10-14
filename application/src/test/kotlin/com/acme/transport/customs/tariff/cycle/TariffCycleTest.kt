package com.acme.transport.customs.tariff.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TariffCycleTest {
    @Test
    fun `starts in requested`() {
        val item = newTariffCycle("code-436", "label-598")
        assertEquals(TariffCycleStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newTariffCycle("code-436", "label-598")
        try {
            item.moveTo(TariffCycleStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TariffCycleStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newTariffCycle("code-436", "label-598")
        item.status = TariffCycleStatus.EXPIRED
        assertFails("move") { item.moveTo(TariffCycleStatus.REQUESTED) }
    }
}
