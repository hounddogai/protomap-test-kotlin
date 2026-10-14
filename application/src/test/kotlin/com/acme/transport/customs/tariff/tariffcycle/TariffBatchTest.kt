package com.acme.transport.customs.tariff.tariffcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TariffBatchTest {
    @Test
    fun `starts in new`() {
        val item = newTariffBatch("code-802", "title-161")
        assertEquals(TariffBatchStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newTariffBatch("code-802", "title-161")
        try {
            item.moveTo(TariffBatchStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TariffBatchStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newTariffBatch("code-802", "title-161")
        item.status = TariffBatchStatus.FAILED
        assertFails("move") { item.moveTo(TariffBatchStatus.NEW) }
    }
}
