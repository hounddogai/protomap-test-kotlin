package com.acme.transport.network.transittime.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictTransitTimeDigestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictTransitTimeDigestStore()
        val item = newStrictTransitTimeDigest("code-99", "title-838")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
