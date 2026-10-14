package com.acme.transport.linehaul.laneplan.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientLanePlanEstimateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientLanePlanEstimateStore()
        val item = newLenientLanePlanEstimate("code-791", "title-945")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
