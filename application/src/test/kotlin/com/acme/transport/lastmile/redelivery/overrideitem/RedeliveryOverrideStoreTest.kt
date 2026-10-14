package com.acme.transport.lastmile.redelivery.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RedeliveryOverrideStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRedeliveryOverrideStore()
        val item = newRedeliveryOverride("code-748", "label-707")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
