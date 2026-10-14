package com.acme.transport.emissions.idling.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class IdlingSnapshotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryIdlingSnapshotStore()
        val item = newIdlingSnapshot("code-222", "title-603")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
