package com.acme.transport.lastmile.locker.lockertier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultLockerMetricStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultLockerMetricStore()
        val item = newDefaultLockerMetric("code-250", "label-942")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
