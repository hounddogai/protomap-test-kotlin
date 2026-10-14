package com.acme.transport.freightpay.ratecard.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RateCardCheckpointStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRateCardCheckpointStore()
        val item = newRateCardCheckpoint("code-719", "label-913")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
