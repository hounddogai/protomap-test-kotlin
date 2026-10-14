package com.acme.transport.customs.tariff.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TariffDraftTest {
    @Test
    fun `starts in planned`() {
        val item = newTariffDraft("code-452", "name-111")
        assertEquals(TariffDraftStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newTariffDraft("code-452", "name-111")
        try {
            item.moveTo(TariffDraftStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TariffDraftStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newTariffDraft("code-452", "name-111")
        item.status = TariffDraftStatus.CANCELLED
        assertFails("move") { item.moveTo(TariffDraftStatus.PLANNED) }
    }
}
