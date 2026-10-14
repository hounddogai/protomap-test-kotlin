package com.acme.transport.lastmile.proofofdelivery.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ProofOfDeliveryScheduleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryProofOfDeliveryScheduleStore()
        val item = newProofOfDeliverySchedule("code-227", "name-715")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
