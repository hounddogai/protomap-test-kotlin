package com.acme.transport.linehaul.laneplan.planbundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualLanePlanBundleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualLanePlanBundleStore()
        val item = newManualLanePlanBundle("code-699", "label-931")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
