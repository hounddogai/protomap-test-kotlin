package com.acme.transport.customs.tariff.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TariffTemplateTest {
    @Test
    fun `starts in planned`() {
        val item = newTariffTemplate("code-57", "name-38")
        assertEquals(TariffTemplateStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newTariffTemplate("code-57", "name-38")
        try {
            item.moveTo(TariffTemplateStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TariffTemplateStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newTariffTemplate("code-57", "name-38")
        item.status = TariffTemplateStatus.CANCELLED
        assertFails("move") { item.moveTo(TariffTemplateStatus.PLANNED) }
    }
}
