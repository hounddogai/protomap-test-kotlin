package com.acme.transport.linehaul.backhaul.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultBackhaulDigestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultBackhaulDigestStore()
        val item = newDefaultBackhaulDigest("code-348", "name-407")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
