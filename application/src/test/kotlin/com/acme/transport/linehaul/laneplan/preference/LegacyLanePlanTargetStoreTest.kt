package com.acme.transport.linehaul.laneplan.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyLanePlanTargetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyLanePlanTargetStore()
        val item = newLegacyLanePlanTarget("code-837", "label-471")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
