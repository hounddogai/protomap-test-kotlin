package com.acme.transport.linehaul.laneplan.planbudget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LanePlanVarianceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLanePlanVarianceStore()
        val item = newLanePlanVariance("code-69", "title-869")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
