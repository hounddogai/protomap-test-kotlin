package com.acme.transport.emissions.idling.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class IdlingBundleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryIdlingBundleStore()
        val item = newIdlingBundle("code-200", "label-375")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
