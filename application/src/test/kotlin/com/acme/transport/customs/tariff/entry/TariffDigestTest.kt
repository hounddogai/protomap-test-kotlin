package com.acme.transport.customs.tariff.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TariffDigestTest {
    @Test
    fun `starts in draft`() {
        val item = newTariffDigest("code-620", "title-677")
        assertEquals(TariffDigestStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newTariffDigest("code-620", "title-677")
        try {
            item.moveTo(TariffDigestStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TariffDigestStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newTariffDigest("code-620", "title-677")
        item.status = TariffDigestStatus.CLOSED
        assertFails("move") { item.moveTo(TariffDigestStatus.DRAFT) }
    }
}
