package com.acme.transport.customs.hold.holdsummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictHoldMappingStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictHoldMappingStore()
        val item = newStrictHoldMapping("code-219", "title-894")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
