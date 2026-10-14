package com.acme.transport.linehaul.laneplan.planreservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveLanePlanBundleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveLanePlanBundleStore()
        val item = newAdaptiveLanePlanBundle("code-847", "label-973")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
