package com.acme.transport.lastmile.redelivery.redeliveryrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredRedeliveryBundleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredRedeliveryBundleStore()
        val item = newTieredRedeliveryBundle("code-168", "name-344")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
