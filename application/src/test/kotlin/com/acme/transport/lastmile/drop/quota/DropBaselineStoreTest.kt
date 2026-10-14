package com.acme.transport.lastmile.drop.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DropBaselineStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDropBaselineStore()
        val item = newDropBaseline("code-685", "label-774")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
