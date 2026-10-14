package com.acme.transport.customs.hold.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedHoldRequestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedHoldRequestStore()
        val item = newCappedHoldRequest("code-264", "title-970")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
