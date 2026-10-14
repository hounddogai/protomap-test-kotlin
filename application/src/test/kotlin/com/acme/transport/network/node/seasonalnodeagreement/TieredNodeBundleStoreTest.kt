package com.acme.transport.network.node.seasonalnodeagreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredNodeBundleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredNodeBundleStore()
        val item = newTieredNodeBundle("code-46", "name-41")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
