package com.acme.transport.crewing.roster.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientRosterBatchStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientRosterBatchStore()
        val item = newLenientRosterBatch("code-94", "label-359")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
