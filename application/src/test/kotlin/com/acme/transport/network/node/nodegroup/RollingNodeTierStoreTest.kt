package com.acme.transport.network.node.nodegroup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingNodeTierStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingNodeTierStore()
        val item = newRollingNodeTier("code-886", "name-278")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
