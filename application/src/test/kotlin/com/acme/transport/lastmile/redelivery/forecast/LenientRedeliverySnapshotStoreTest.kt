package com.acme.transport.lastmile.redelivery.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientRedeliverySnapshotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientRedeliverySnapshotStore()
        val item = newLenientRedeliverySnapshot("code-209", "label-257")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
