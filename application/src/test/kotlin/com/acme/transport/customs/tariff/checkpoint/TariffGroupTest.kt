package com.acme.transport.customs.tariff.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TariffGroupTest {
    @Test
    fun `starts in requested`() {
        val item = newTariffGroup("code-431", "name-21")
        assertEquals(TariffGroupStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newTariffGroup("code-431", "name-21")
        try {
            item.moveTo(TariffGroupStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TariffGroupStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newTariffGroup("code-431", "name-21")
        item.status = TariffGroupStatus.EXPIRED
        assertFails("move") { item.moveTo(TariffGroupStatus.REQUESTED) }
    }
}
