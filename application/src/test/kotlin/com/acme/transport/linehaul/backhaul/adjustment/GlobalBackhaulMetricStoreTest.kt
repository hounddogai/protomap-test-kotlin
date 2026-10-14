package com.acme.transport.linehaul.backhaul.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalBackhaulMetricStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalBackhaulMetricStore()
        val item = newGlobalBackhaulMetric("code-441", "label-589")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
