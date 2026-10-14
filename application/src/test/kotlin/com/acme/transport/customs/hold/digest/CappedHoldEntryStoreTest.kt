package com.acme.transport.customs.hold.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedHoldEntryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedHoldEntryStore()
        val item = newCappedHoldEntry("code-950", "title-140")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
