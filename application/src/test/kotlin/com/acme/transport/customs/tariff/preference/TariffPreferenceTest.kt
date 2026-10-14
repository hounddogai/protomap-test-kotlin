package com.acme.transport.customs.tariff.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TariffPreferenceTest {
    @Test
    fun `starts in requested`() {
        val item = newTariffPreference("code-489", "name-883")
        assertEquals(TariffPreferenceStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newTariffPreference("code-489", "name-883")
        try {
            item.moveTo(TariffPreferenceStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TariffPreferenceStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newTariffPreference("code-489", "name-883")
        item.status = TariffPreferenceStatus.EXPIRED
        assertFails("move") { item.moveTo(TariffPreferenceStatus.REQUESTED) }
    }
}
