package com.acme.transport.lastmile.drop.droprequest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientDropRequestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientDropRequestStore()
        val item = newLenientDropRequest("code-901", "title-874")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
