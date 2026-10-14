package com.acme.transport.safety.cameraevent.evententry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryCameraEventDigestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryCameraEventDigestStore()
        val item = newPrimaryCameraEventDigest("code-912", "title-16")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
