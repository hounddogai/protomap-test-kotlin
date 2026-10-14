package com.acme.transport.freightpay.ratecard.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalRateCardCheckpointStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalRateCardCheckpointStore()
        val item = newRegionalRateCardCheckpoint("code-363", "title-238")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
