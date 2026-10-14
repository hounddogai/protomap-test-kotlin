package com.acme.transport.linehaul.backhaul.primarybackhaulvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryBackhaulMetricStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryBackhaulMetricStore()
        val item = newPrimaryBackhaulMetric("code-944", "label-254")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
