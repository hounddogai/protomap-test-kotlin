package com.acme.transport.lastmile.redelivery.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalRedeliverySnapshotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalRedeliverySnapshotStore()
        val item = newGlobalRedeliverySnapshot("code-752", "name-877")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
