package com.acme.transport.customs.tariff.tariffpolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TariffTargetTest {
    @Test
    fun `starts in created`() {
        val item = newTariffTarget("code-598", "title-443")
        assertEquals(TariffTargetStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newTariffTarget("code-598", "title-443")
        try {
            item.moveTo(TariffTargetStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TariffTargetStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newTariffTarget("code-598", "title-443")
        item.status = TariffTargetStatus.RETIRED
        assertFails("move") { item.moveTo(TariffTargetStatus.CREATED) }
    }
}
