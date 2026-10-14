package com.acme.transport.customs.hold.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class HoldRequestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryHoldRequestStore()
        val item = newHoldRequest("code-162", "name-679")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
