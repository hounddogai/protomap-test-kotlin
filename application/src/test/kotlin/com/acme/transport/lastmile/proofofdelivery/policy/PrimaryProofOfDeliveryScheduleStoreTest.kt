package com.acme.transport.lastmile.proofofdelivery.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryProofOfDeliveryScheduleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryProofOfDeliveryScheduleStore()
        val item = newPrimaryProofOfDeliverySchedule("code-825", "label-718")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
