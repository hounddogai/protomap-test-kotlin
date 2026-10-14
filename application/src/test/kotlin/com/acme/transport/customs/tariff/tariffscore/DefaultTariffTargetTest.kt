package com.acme.transport.customs.tariff.tariffscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultTariffTargetTest {
    @Test
    fun `starts in created`() {
        val item = newDefaultTariffTarget("code-192", "title-381")
        assertEquals(DefaultTariffTargetStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newDefaultTariffTarget("code-192", "title-381")
        try {
            item.moveTo(DefaultTariffTargetStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultTariffTargetStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newDefaultTariffTarget("code-192", "title-381")
        item.status = DefaultTariffTargetStatus.RETIRED
        assertFails("move") { item.moveTo(DefaultTariffTargetStatus.CREATED) }
    }
}
