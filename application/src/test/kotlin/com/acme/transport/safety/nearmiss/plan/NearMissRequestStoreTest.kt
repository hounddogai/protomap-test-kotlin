package com.acme.transport.safety.nearmiss.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class NearMissRequestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryNearMissRequestStore()
        val item = newNearMissRequest("code-373", "label-291")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
