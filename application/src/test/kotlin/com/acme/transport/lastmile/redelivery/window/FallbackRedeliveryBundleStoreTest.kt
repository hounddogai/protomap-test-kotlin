package com.acme.transport.lastmile.redelivery.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackRedeliveryBundleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackRedeliveryBundleStore()
        val item = newFallbackRedeliveryBundle("code-402", "label-601")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
