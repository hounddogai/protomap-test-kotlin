package com.acme.transport.lastmile.locker.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualLockerMetricStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualLockerMetricStore()
        val item = newManualLockerMetric("code-296", "title-237")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
