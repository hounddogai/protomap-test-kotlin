package com.acme.transport.crewing.restbreak.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RestBreakGroupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRestBreakGroupStore()
        val item = newRestBreakGroup("code-912", "label-695")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
