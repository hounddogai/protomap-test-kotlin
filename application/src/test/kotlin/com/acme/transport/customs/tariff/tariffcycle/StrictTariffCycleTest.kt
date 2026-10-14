package com.acme.transport.customs.tariff.tariffcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictTariffCycleTest {
    @Test
    fun `starts in planned`() {
        val item = newStrictTariffCycle("code-670", "title-209")
        assertEquals(StrictTariffCycleStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newStrictTariffCycle("code-670", "title-209")
        try {
            item.moveTo(StrictTariffCycleStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictTariffCycleStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newStrictTariffCycle("code-670", "title-209")
        item.status = StrictTariffCycleStatus.CANCELLED
        assertFails("move") { item.moveTo(StrictTariffCycleStatus.PLANNED) }
    }
}
