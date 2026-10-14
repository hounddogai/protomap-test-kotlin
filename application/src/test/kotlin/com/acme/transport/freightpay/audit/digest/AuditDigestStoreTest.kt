package com.acme.transport.freightpay.audit.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AuditDigestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAuditDigestStore()
        val item = newAuditDigest("code-938", "label-795")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
