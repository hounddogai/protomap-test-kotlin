package com.acme.transport.network.transittime.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictTransitTimeBaselineStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictTransitTimeBaselineStore()
        val item = newStrictTransitTimeBaseline("code-251", "label-943")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
