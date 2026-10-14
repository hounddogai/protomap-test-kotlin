package com.acme.transport.lastmile.redelivery.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredRedeliveryOverrideStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredRedeliveryOverrideStore()
        val item = newTieredRedeliveryOverride("code-969", "name-555")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
