package com.acme.transport.linehaul.backhaul.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedBackhaulPolicyStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedBackhaulPolicyStore()
        val item = newCappedBackhaulPolicy("code-425", "title-371")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
