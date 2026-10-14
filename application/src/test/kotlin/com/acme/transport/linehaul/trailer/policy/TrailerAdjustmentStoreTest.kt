package com.acme.transport.linehaul.trailer.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TrailerAdjustmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTrailerAdjustmentStore()
        val item = newTrailerAdjustment("code-451", "title-968")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
