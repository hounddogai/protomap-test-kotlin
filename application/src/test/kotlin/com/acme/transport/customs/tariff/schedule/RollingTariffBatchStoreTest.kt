package com.acme.transport.customs.tariff.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingTariffBatchStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingTariffBatchStore()
        val item = newRollingTariffBatch("code-607", "title-608")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
