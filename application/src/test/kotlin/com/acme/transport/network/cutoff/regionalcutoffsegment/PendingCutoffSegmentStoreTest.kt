package com.acme.transport.network.cutoff.regionalcutoffsegment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingCutoffSegmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingCutoffSegmentStore()
        val item = newPendingCutoffSegment("code-46", "name-155")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
