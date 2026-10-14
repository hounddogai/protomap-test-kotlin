package com.acme.transport.customs.hold.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class HoldBaselineStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryHoldBaselineStore()
        val item = newHoldBaseline("code-710", "title-565")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
