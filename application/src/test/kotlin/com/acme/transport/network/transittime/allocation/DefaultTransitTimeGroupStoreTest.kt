package com.acme.transport.network.transittime.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultTransitTimeGroupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultTransitTimeGroupStore()
        val item = newDefaultTransitTimeGroup("code-327", "label-581")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
