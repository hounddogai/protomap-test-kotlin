package com.acme.transport.safety.nearmiss.misscycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientNearMissBatchStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientNearMissBatchStore()
        val item = newLenientNearMissBatch("code-518", "title-324")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
