package com.acme.transport.linehaul.capacity.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedCapacityAgreementStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedCapacityAgreementStore()
        val item = newBatchedCapacityAgreement("code-810", "title-81")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
