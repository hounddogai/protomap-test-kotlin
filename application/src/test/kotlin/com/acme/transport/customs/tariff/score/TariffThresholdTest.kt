package com.acme.transport.customs.tariff.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TariffThresholdTest {
    @Test
    fun `starts in idle`() {
        val item = newTariffThreshold("code-270", "name-718")
        assertEquals(TariffThresholdStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newTariffThreshold("code-270", "name-718")
        try {
            item.moveTo(TariffThresholdStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TariffThresholdStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newTariffThreshold("code-270", "name-718")
        item.status = TariffThresholdStatus.STOPPED
        assertFails("move") { item.moveTo(TariffThresholdStatus.IDLE) }
    }
}
