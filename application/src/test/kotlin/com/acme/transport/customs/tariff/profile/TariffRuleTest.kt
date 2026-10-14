package com.acme.transport.customs.tariff.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TariffRuleTest {
    @Test
    fun `starts in draft`() {
        val item = newTariffRule("code-473", "title-518")
        assertEquals(TariffRuleStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newTariffRule("code-473", "title-518")
        try {
            item.moveTo(TariffRuleStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TariffRuleStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newTariffRule("code-473", "title-518")
        item.status = TariffRuleStatus.CLOSED
        assertFails("move") { item.moveTo(TariffRuleStatus.DRAFT) }
    }
}
