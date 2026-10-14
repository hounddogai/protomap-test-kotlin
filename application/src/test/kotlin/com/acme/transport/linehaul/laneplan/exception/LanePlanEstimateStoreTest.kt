package com.acme.transport.linehaul.laneplan.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LanePlanEstimateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLanePlanEstimateStore()
        val item = newLanePlanEstimate("code-425", "label-234")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
