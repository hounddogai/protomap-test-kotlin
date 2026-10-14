package com.acme.transport.customs.tariff.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TariffEntryTest {
    @Test
    fun `starts in new`() {
        val item = newTariffEntry("code-226", "label-773")
        assertEquals(TariffEntryStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newTariffEntry("code-226", "label-773")
        try {
            item.moveTo(TariffEntryStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TariffEntryStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newTariffEntry("code-226", "label-773")
        item.status = TariffEntryStatus.FAILED
        assertFails("move") { item.moveTo(TariffEntryStatus.NEW) }
    }
}
