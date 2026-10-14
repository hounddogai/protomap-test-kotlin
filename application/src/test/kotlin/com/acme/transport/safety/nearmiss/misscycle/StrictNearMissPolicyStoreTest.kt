package com.acme.transport.safety.nearmiss.misscycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictNearMissPolicyStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictNearMissPolicyStore()
        val item = newStrictNearMissPolicy("code-9", "label-104")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
