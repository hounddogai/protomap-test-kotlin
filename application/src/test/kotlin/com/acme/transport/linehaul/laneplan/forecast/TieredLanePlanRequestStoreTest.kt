package com.acme.transport.linehaul.laneplan.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredLanePlanRequestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredLanePlanRequestStore()
        val item = newTieredLanePlanRequest("code-835", "label-22")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
