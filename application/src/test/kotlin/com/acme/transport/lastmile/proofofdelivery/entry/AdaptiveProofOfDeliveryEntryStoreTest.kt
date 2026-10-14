package com.acme.transport.lastmile.proofofdelivery.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveProofOfDeliveryEntryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveProofOfDeliveryEntryStore()
        val item = newAdaptiveProofOfDeliveryEntry("code-877", "title-679")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
