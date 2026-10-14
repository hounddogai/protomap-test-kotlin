package com.acme.transport.customs.tariff.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultTariffDraftTest {
    @Test
    fun `starts in created`() {
        val item = newDefaultTariffDraft("code-207", "title-17")
        assertEquals(DefaultTariffDraftStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newDefaultTariffDraft("code-207", "title-17")
        try {
            item.moveTo(DefaultTariffDraftStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultTariffDraftStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newDefaultTariffDraft("code-207", "title-17")
        item.status = DefaultTariffDraftStatus.RETIRED
        assertFails("move") { item.moveTo(DefaultTariffDraftStatus.CREATED) }
    }
}
