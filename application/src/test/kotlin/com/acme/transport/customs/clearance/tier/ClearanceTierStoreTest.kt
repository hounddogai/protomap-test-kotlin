package com.acme.transport.customs.clearance.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ClearanceTierStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryClearanceTierStore()
        val item = newClearanceTier("code-544", "label-106")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
