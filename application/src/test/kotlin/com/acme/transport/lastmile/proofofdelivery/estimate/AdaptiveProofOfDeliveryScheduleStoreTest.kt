package com.acme.transport.lastmile.proofofdelivery.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveProofOfDeliveryScheduleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveProofOfDeliveryScheduleStore()
        val item = newAdaptiveProofOfDeliverySchedule("code-212", "name-735")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
