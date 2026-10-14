package com.acme.transport.customs.hold.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class HoldDigestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryHoldDigestStore()
        val item = newHoldDigest("code-146", "label-679")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
