package com.acme.transport.customs.tariff.tariffbatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedTariffVarianceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedTariffVarianceStore()
        val item = newBatchedTariffVariance("code-44", "title-90")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
