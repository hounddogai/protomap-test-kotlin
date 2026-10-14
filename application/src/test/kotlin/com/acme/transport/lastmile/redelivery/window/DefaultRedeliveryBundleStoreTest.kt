package com.acme.transport.lastmile.redelivery.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultRedeliveryBundleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultRedeliveryBundleStore()
        val item = newDefaultRedeliveryBundle("code-943", "name-706")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
