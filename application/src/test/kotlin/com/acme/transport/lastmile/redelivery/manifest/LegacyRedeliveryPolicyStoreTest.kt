package com.acme.transport.lastmile.redelivery.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyRedeliveryPolicyStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyRedeliveryPolicyStore()
        val item = newLegacyRedeliveryPolicy("code-268", "label-982")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
