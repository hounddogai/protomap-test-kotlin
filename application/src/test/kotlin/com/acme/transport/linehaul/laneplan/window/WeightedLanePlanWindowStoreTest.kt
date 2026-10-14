package com.acme.transport.linehaul.laneplan.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedLanePlanWindowStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedLanePlanWindowStore()
        val item = newWeightedLanePlanWindow("code-418", "name-65")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
