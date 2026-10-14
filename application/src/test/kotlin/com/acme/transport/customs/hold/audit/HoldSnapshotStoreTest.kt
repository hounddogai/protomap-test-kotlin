package com.acme.transport.customs.hold.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class HoldSnapshotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryHoldSnapshotStore()
        val item = newHoldSnapshot("code-237", "label-901")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
