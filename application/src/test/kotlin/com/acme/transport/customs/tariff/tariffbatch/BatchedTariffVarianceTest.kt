package com.acme.transport.customs.tariff.tariffbatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedTariffVarianceTest {
    @Test
    fun `starts in new`() {
        val item = newBatchedTariffVariance("code-44", "title-90")
        assertEquals(BatchedTariffVarianceStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newBatchedTariffVariance("code-44", "title-90")
        try {
            item.moveTo(BatchedTariffVarianceStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedTariffVarianceStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newBatchedTariffVariance("code-44", "title-90")
        item.status = BatchedTariffVarianceStatus.FAILED
        assertFails("move") { item.moveTo(BatchedTariffVarianceStatus.NEW) }
    }
}
