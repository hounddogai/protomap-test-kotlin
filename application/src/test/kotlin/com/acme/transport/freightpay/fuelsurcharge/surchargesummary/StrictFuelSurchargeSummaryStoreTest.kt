package com.acme.transport.freightpay.fuelsurcharge.surchargesummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictFuelSurchargeSummaryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictFuelSurchargeSummaryStore()
        val item = newStrictFuelSurchargeSummary("code-839", "title-919")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
