package com.acme.transport.linehaul.backhaul.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BackhaulOverrideStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBackhaulOverrideStore()
        val item = newBackhaulOverride("code-987", "name-929")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
