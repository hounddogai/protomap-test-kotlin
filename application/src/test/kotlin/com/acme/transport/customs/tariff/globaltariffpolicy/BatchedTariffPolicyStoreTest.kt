package com.acme.transport.customs.tariff.globaltariffpolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedTariffPolicyStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedTariffPolicyStore()
        val item = newBatchedTariffPolicy("code-171", "name-893")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
