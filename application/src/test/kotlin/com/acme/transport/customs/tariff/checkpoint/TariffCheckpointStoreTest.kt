package com.acme.transport.customs.tariff.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TariffCheckpointStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTariffCheckpointStore()
        val item = newTariffCheckpoint("code-481", "label-912")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
