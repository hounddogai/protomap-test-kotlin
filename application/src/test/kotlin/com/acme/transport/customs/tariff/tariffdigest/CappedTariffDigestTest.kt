package com.acme.transport.customs.tariff.tariffdigest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedTariffDigestTest {
    @Test
    fun `starts in idle`() {
        val item = newCappedTariffDigest("code-555", "title-871")
        assertEquals(CappedTariffDigestStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newCappedTariffDigest("code-555", "title-871")
        try {
            item.moveTo(CappedTariffDigestStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedTariffDigestStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newCappedTariffDigest("code-555", "title-871")
        item.status = CappedTariffDigestStatus.STOPPED
        assertFails("move") { item.moveTo(CappedTariffDigestStatus.IDLE) }
    }
}
