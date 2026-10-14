package com.acme.transport.linehaul.backhaul.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BackhaulClaimStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBackhaulClaimStore()
        val item = newBackhaulClaim("code-751", "name-191")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
