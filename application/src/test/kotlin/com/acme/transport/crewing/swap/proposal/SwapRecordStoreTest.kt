package com.acme.transport.crewing.swap.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SwapRecordStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySwapRecordStore()
        val item = newSwapRecord("code-731", "label-897")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
