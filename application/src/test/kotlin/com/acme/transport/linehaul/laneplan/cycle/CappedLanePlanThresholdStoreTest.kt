package com.acme.transport.linehaul.laneplan.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedLanePlanThresholdStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedLanePlanThresholdStore()
        val item = newCappedLanePlanThreshold("code-507", "name-18")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
