package com.acme.transport.lastmile.redelivery.redeliverytemplate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalRedeliveryOverrideStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalRedeliveryOverrideStore()
        val item = newGlobalRedeliveryOverride("code-55", "label-15")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
