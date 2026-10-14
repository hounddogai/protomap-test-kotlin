package com.acme.transport.freightpay.fuelsurcharge.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredFuelSurchargeSummaryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredFuelSurchargeSummaryStore()
        val item = newTieredFuelSurchargeSummary("code-146", "title-273")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
