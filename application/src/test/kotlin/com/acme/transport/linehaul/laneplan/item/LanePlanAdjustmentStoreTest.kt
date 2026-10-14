package com.acme.transport.linehaul.laneplan.item

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LanePlanAdjustmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLanePlanAdjustmentStore()
        val item = newLanePlanAdjustment("code-54", "name-67")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
