package com.acme.transport.customs.hold.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedHoldRollupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedHoldRollupStore()
        val item = newCappedHoldRollup("code-69", "title-449")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
