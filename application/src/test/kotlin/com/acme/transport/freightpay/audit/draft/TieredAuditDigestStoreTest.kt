package com.acme.transport.freightpay.audit.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredAuditDigestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredAuditDigestStore()
        val item = newTieredAuditDigest("code-460", "name-781")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
