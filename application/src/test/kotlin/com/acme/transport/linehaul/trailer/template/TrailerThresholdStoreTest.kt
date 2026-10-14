package com.acme.transport.linehaul.trailer.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TrailerThresholdStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTrailerThresholdStore()
        val item = newTrailerThreshold("code-746", "name-23")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
