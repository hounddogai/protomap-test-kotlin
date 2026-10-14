package com.acme.transport.fleetops.tire.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingTireTargetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingTireTargetStore()
        val item = newPendingTireTarget("code-513", "label-327")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
