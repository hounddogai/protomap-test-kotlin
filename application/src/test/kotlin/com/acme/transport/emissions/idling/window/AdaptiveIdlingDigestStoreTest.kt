package com.acme.transport.emissions.idling.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveIdlingDigestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveIdlingDigestStore()
        val item = newAdaptiveIdlingDigest("code-776", "title-174")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
