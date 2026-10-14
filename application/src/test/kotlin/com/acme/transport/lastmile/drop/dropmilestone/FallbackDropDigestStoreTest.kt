package com.acme.transport.lastmile.drop.dropmilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackDropDigestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackDropDigestStore()
        val item = newFallbackDropDigest("code-596", "label-489")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
