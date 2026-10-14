package com.acme.transport.linehaul.laneplan.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LanePlanThresholdStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLanePlanThresholdStore()
        val item = newLanePlanThreshold("code-327", "title-909")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
