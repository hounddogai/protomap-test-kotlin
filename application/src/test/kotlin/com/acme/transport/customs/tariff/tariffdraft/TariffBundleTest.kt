package com.acme.transport.customs.tariff.tariffdraft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TariffBundleTest {
    @Test
    fun `starts in requested`() {
        val item = newTariffBundle("code-870", "title-950")
        assertEquals(TariffBundleStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newTariffBundle("code-870", "title-950")
        try {
            item.moveTo(TariffBundleStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TariffBundleStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newTariffBundle("code-870", "title-950")
        item.status = TariffBundleStatus.EXPIRED
        assertFails("move") { item.moveTo(TariffBundleStatus.REQUESTED) }
    }
}
