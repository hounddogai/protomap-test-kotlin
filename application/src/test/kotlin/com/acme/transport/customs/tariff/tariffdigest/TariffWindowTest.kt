package com.acme.transport.customs.tariff.tariffdigest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TariffWindowTest {
    @Test
    fun `starts in proposed`() {
        val item = newTariffWindow("code-106", "label-936")
        assertEquals(TariffWindowStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newTariffWindow("code-106", "label-936")
        try {
            item.moveTo(TariffWindowStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TariffWindowStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newTariffWindow("code-106", "label-936")
        item.status = TariffWindowStatus.WITHDRAWN
        assertFails("move") { item.moveTo(TariffWindowStatus.PROPOSED) }
    }
}
