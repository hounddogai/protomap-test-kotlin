package com.acme.transport.network.node.nodeagreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingNodeAgreementStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingNodeAgreementStore()
        val item = newPendingNodeAgreement("code-608", "label-338")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
