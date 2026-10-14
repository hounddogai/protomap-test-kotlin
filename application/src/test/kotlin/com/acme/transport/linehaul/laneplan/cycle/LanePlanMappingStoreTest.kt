package com.acme.transport.linehaul.laneplan.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LanePlanMappingStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLanePlanMappingStore()
        val item = newLanePlanMapping("code-578", "label-820")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
