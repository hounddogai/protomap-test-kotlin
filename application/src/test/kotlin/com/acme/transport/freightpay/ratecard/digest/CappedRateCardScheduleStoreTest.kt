package com.acme.transport.freightpay.ratecard.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedRateCardScheduleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedRateCardScheduleStore()
        val item = newCappedRateCardSchedule("code-675", "name-958")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
