package com.acme.transport.customs.tariff.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TariffProfileTest {
    @Test
    fun `starts in open`() {
        val item = newTariffProfile("code-849", "label-103")
        assertEquals(TariffProfileStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newTariffProfile("code-849", "label-103")
        try {
            item.moveTo(TariffProfileStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TariffProfileStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newTariffProfile("code-849", "label-103")
        item.status = TariffProfileStatus.ARCHIVED
        assertFails("move") { item.moveTo(TariffProfileStatus.OPEN) }
    }
}
