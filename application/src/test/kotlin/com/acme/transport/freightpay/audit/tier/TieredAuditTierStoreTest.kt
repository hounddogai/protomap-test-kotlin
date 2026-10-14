package com.acme.transport.freightpay.audit.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredAuditTierStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredAuditTierStore()
        val item = newTieredAuditTier("code-907", "name-875")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
