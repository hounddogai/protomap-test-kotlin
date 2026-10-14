package com.acme.transport.emissions.evcharge.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingEvChargeOverrideStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingEvChargeOverrideStore()
        val item = newPendingEvChargeOverride("code-557", "label-897")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
