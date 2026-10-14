package com.acme.transport.crewing.swap.swaprecord

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedSwapRecordStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedSwapRecordStore()
        val item = newCappedSwapRecord("code-689", "name-49")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
