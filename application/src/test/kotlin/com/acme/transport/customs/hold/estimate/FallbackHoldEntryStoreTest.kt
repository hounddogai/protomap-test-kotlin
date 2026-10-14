package com.acme.transport.customs.hold.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackHoldEntryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackHoldEntryStore()
        val item = newFallbackHoldEntry("code-910", "title-247")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
