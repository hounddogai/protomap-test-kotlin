package com.acme.transport.customs.tariff.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TariffBaselineTest {
    @Test
    fun `starts in new`() {
        val item = newTariffBaseline("code-800", "title-585")
        assertEquals(TariffBaselineStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newTariffBaseline("code-800", "title-585")
        try {
            item.moveTo(TariffBaselineStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TariffBaselineStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newTariffBaseline("code-800", "title-585")
        item.status = TariffBaselineStatus.FAILED
        assertFails("move") { item.moveTo(TariffBaselineStatus.NEW) }
    }
}
