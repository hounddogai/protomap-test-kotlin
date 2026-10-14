package com.acme.transport.freightpay.accessorial.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedAccessorialBaselineStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedAccessorialBaselineStore()
        val item = newBatchedAccessorialBaseline("code-345", "label-683")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
