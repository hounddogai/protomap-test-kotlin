package com.acme.transport.customs.tariff.tariffprofile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientTariffEntryTest {
    @Test
    fun `starts in requested`() {
        val item = newLenientTariffEntry("code-454", "label-425")
        assertEquals(LenientTariffEntryStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newLenientTariffEntry("code-454", "label-425")
        try {
            item.moveTo(LenientTariffEntryStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientTariffEntryStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newLenientTariffEntry("code-454", "label-425")
        item.status = LenientTariffEntryStatus.EXPIRED
        assertFails("move") { item.moveTo(LenientTariffEntryStatus.REQUESTED) }
    }
}
