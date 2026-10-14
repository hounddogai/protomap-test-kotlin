package com.acme.transport.freightpay.audit.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveAuditManifestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveAuditManifestStore()
        val item = newAdaptiveAuditManifest("code-544", "name-547")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
