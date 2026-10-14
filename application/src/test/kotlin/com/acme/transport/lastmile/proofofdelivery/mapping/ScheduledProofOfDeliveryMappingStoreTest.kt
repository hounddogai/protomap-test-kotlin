package com.acme.transport.lastmile.proofofdelivery.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledProofOfDeliveryMappingStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledProofOfDeliveryMappingStore()
        val item = newScheduledProofOfDeliveryMapping("code-118", "name-877")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
