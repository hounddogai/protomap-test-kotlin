package com.acme.transport.linehaul.laneplan.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictLanePlanMappingStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictLanePlanMappingStore()
        val item = newStrictLanePlanMapping("code-634", "title-459")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
