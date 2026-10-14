package com.acme.transport.lastmile.locker.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LockerMetricStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLockerMetricStore()
        val item = newLockerMetric("code-182", "title-692")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
