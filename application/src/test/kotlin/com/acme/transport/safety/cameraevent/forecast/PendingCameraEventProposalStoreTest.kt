package com.acme.transport.safety.cameraevent.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingCameraEventProposalStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingCameraEventProposalStore()
        val item = newPendingCameraEventProposal("code-197", "name-683")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
