package com.acme.transport.safety.nearmiss.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class NearMissOverrideStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryNearMissOverrideStore()
        val item = newNearMissOverride("code-184", "label-911")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
