package com.acme.transport.customs.tariff.tariffoverride

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedTariffCheckpointStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedTariffCheckpointStore()
        val item = newBatchedTariffCheckpoint("code-298", "label-217")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
