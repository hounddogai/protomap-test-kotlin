package com.acme.transport.customs.tariff.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredTariffTemplateTest {
    @Test
    fun `starts in created`() {
        val item = newTieredTariffTemplate("code-134", "title-510")
        assertEquals(TieredTariffTemplateStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newTieredTariffTemplate("code-134", "title-510")
        try {
            item.moveTo(TieredTariffTemplateStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredTariffTemplateStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newTieredTariffTemplate("code-134", "title-510")
        item.status = TieredTariffTemplateStatus.RETIRED
        assertFails("move") { item.moveTo(TieredTariffTemplateStatus.CREATED) }
    }
}
