package com.acme.transport.customs.tariff.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackTariffBaselineTest {
    @Test
    fun `starts in idle`() {
        val item = newFallbackTariffBaseline("code-485", "name-619")
        assertEquals(FallbackTariffBaselineStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newFallbackTariffBaseline("code-485", "name-619")
        try {
            item.moveTo(FallbackTariffBaselineStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackTariffBaselineStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newFallbackTariffBaseline("code-485", "name-619")
        item.status = FallbackTariffBaselineStatus.STOPPED
        assertFails("move") { item.moveTo(FallbackTariffBaselineStatus.IDLE) }
    }
}
