package com.acme.transport.network.transittime.timerequest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientTransitTimeRequestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientTransitTimeRequestStore()
        val item = newLenientTransitTimeRequest("code-604", "title-272")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
