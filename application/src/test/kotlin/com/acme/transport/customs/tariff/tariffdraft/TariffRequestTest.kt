package com.acme.transport.customs.tariff.tariffdraft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TariffRequestTest {
    @Test
    fun `starts in draft`() {
        val item = newTariffRequest("code-325", "title-144")
        assertEquals(TariffRequestStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newTariffRequest("code-325", "title-144")
        try {
            item.moveTo(TariffRequestStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TariffRequestStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newTariffRequest("code-325", "title-144")
        item.status = TariffRequestStatus.CLOSED
        assertFails("move") { item.moveTo(TariffRequestStatus.DRAFT) }
    }
}
