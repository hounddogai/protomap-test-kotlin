package com.acme.transport.lastmile.proofofdelivery.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledProofOfDeliveryEntryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledProofOfDeliveryEntryStore()
        val item = newScheduledProofOfDeliveryEntry("code-705", "name-481")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
