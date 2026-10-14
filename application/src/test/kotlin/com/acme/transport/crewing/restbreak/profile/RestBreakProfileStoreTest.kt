package com.acme.transport.crewing.restbreak.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RestBreakProfileStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRestBreakProfileStore()
        val item = newRestBreakProfile("code-838", "name-947")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
