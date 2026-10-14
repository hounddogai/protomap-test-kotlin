package com.acme.transport.safety.nearmiss.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class NearMissRecordStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryNearMissRecordStore()
        val item = newNearMissRecord("code-553", "title-908")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
