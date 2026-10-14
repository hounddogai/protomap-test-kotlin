package com.acme.transport.freightpay.accessorial.accessorialentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedAccessorialEntryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedAccessorialEntryStore()
        val item = newCappedAccessorialEntry("code-746", "name-804")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
