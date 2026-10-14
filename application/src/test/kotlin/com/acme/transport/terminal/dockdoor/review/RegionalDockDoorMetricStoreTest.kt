package com.acme.transport.terminal.dockdoor.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalDockDoorMetricStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalDockDoorMetricStore()
        val item = newRegionalDockDoorMetric("code-414", "title-368")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
