package com.acme.transport.customs.tariff.tariffschedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackTariffScheduleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackTariffScheduleStore()
        val item = newFallbackTariffSchedule("code-840", "name-157")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
