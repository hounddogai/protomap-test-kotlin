package com.acme.transport.network.hub.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingHubLedgerStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingHubLedgerStore()
        val item = newRollingHubLedger("code-692", "name-235")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
