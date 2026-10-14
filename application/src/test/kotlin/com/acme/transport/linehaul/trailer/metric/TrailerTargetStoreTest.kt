package com.acme.transport.linehaul.trailer.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TrailerTargetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTrailerTargetStore()
        val item = newTrailerTarget("code-349", "label-502")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
