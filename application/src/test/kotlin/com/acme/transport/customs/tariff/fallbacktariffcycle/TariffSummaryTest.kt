package com.acme.transport.customs.tariff.fallbacktariffcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TariffSummaryTest {
    @Test
    fun `starts in draft`() {
        val item = newTariffSummary("code-80", "name-809")
        assertEquals(TariffSummaryStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newTariffSummary("code-80", "name-809")
        try {
            item.moveTo(TariffSummaryStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TariffSummaryStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newTariffSummary("code-80", "name-809")
        item.status = TariffSummaryStatus.CLOSED
        assertFails("move") { item.moveTo(TariffSummaryStatus.DRAFT) }
    }
}
