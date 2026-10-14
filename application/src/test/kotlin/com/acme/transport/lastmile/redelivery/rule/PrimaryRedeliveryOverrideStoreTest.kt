package com.acme.transport.lastmile.redelivery.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryRedeliveryOverrideStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryRedeliveryOverrideStore()
        val item = newPrimaryRedeliveryOverride("code-735", "label-905")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
