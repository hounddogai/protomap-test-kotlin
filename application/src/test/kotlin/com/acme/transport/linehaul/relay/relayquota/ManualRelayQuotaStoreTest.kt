package com.acme.transport.linehaul.relay.relayquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualRelayQuotaStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualRelayQuotaStore()
        val item = newManualRelayQuota("code-125", "title-791")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
