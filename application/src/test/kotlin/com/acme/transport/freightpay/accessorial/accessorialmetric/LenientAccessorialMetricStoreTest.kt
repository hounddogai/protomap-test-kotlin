package com.acme.transport.freightpay.accessorial.accessorialmetric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientAccessorialMetricStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientAccessorialMetricStore()
        val item = newLenientAccessorialMetric("code-568", "label-392")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
