package com.acme.transport.network.node.seasonalnodeagreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class NodeEstimateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryNodeEstimateStore()
        val item = newNodeEstimate("code-858", "label-72")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
