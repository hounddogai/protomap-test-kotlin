package com.acme.transport.freightpay.dispute.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveDisputeManifestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveDisputeManifestStore()
        val item = newAdaptiveDisputeManifest("code-71", "name-458")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
