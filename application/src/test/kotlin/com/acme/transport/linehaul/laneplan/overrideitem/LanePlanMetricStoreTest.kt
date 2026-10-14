package com.acme.transport.linehaul.laneplan.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LanePlanMetricStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLanePlanMetricStore()
        val item = newLanePlanMetric("code-58", "name-679")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
